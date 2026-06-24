package com.example.campus_navigation_backend.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.TestcontainersConfiguration;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Shared PostGIS Testcontainers setup for database integration tests.
 */
public abstract class PostgisTestContainerSupport {

    private static final String WINDOWS_DOCKER_HOST = "tcp://127.0.0.1:2375";
    private static final String DOCKER_API_VERSION = "1.43";
    private static final String DOCKER_CLIENT_STRATEGY =
            "org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy";
    private static final String DATABASE_NAME = "navigation_test";
    private static final String DATABASE_USERNAME = "test";
    private static final String DATABASE_PASSWORD = "test";
    private static final String CLI_CONTAINER_NAME = "campus-navigation-postgis-test";
    private static String cliJdbcUrl;

    static {
        configureDockerForCurrentTestJvm();
    }

    static final PostgreSQLContainer<?> POSTGIS = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD)
            .withInitScript("sql/navigation-test-data.sql");

    /**
     * Registers Spring datasource and navigation properties from the running container.
     *
     * @param registry dynamic property registry
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        startPostgisContainer();

        registry.add("spring.datasource.url", PostgisTestContainerSupport::getJdbcUrl);
        registry.add("spring.datasource.username", () -> DATABASE_USERNAME);
        registry.add("spring.datasource.password", () -> DATABASE_PASSWORD);
        registry.add("navigation.request-srid", () -> 4326);
        registry.add("navigation.metric-srid", () -> 4326);
        registry.add("navigation.candidate-radius-meters", () -> 0.01);
        registry.add("navigation.max-start-candidates", () -> 20);
        registry.add("navigation.walkable-highways[0]", () -> "footway");
        registry.add("navigation.walkable-highways[1]", () -> "pedestrian");
        registry.add("navigation.walkable-highways[2]", () -> "steps");
        registry.add("navigation.walkable-highways[3]", () -> "path");
        registry.add("navigation.walkable-highways[4]", () -> "corridor");
        registry.add("navigation.walkable-highways[5]", () -> "living_street");
    }

    /**
     * Starts the PostGIS container before Spring reads datasource properties.
     */
    private static synchronized void startPostgisContainer() {
        if (isWindows()) {
            startPostgisContainerWithDockerCli();
            return;
        }

        if (!POSTGIS.isRunning()) {
            try {
                configureTestcontainersRuntime();
                assertDockerEndpointReachable();
                DockerClientFactory.instance().client();
                POSTGIS.start();
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Docker daemon에 연결할 수 없습니다. Docker Desktop이 실행 중인지 확인하고, "
                                + "Windows IntelliJ에서는 Docker Desktop의 "
                                + "'Expose daemon on tcp://localhost:2375 without TLS' 옵션을 켠 뒤 다시 실행하세요.",
                        e
                );
            }
        }
    }

    private static String getJdbcUrl() {
        if (cliJdbcUrl != null) {
            return cliJdbcUrl;
        }
        return POSTGIS.getJdbcUrl();
    }

    /**
     * Isolates this test JVM from the user's global Testcontainers configuration.
     */
    private static void configureDockerForCurrentTestJvm() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (!osName.contains("win")) {
            return;
        }

        if (System.getenv("DOCKER_HOST") != null || System.getProperty("docker.host") != null) {
            return;
        }

        isolateTestcontainersUserHome();
        System.setProperty("docker.client.strategy", DOCKER_CLIENT_STRATEGY);
        System.setProperty("docker.host", WINDOWS_DOCKER_HOST);
        System.setProperty("docker.api.version", DOCKER_API_VERSION);
    }

    /**
     * Makes Testcontainers read a test-only .testcontainers.properties file.
     */
    private static void isolateTestcontainersUserHome() {
        try {
            Path testHome = Path.of("target", "testcontainers-home").toAbsolutePath();
            Files.createDirectories(testHome);
            Files.write(
                    testHome.resolve(".testcontainers.properties"),
                    List.of(
                            "docker.client.strategy=" + DOCKER_CLIENT_STRATEGY,
                            "docker.host=" + WINDOWS_DOCKER_HOST,
                            "docker.api.version=" + DOCKER_API_VERSION
                    )
            );
            System.setProperty("user.home", testHome.toString());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to prepare isolated Testcontainers user home.", e);
        }
    }

    /**
     * Overrides a user-level Testcontainers strategy inside this test JVM only.
     */
    private static void configureTestcontainersRuntime() {
        TestcontainersConfiguration configuration = TestcontainersConfiguration.getInstance();
        configuration.getUserProperties().setProperty("docker.client.strategy", DOCKER_CLIENT_STRATEGY);
        configuration.getUserProperties().setProperty("docker.host", WINDOWS_DOCKER_HOST);
        configuration.getUserProperties().setProperty("docker.api.version", DOCKER_API_VERSION);

        System.setProperty("docker.client.strategy", DOCKER_CLIENT_STRATEGY);
        System.setProperty("docker.host", WINDOWS_DOCKER_HOST);
        System.setProperty("docker.api.version", DOCKER_API_VERSION);
    }

    /**
     * Fails fast when Docker Desktop's TCP endpoint is not exposed.
     */
    private static void assertDockerEndpointReachable() {
        if (!WINDOWS_DOCKER_HOST.equals(System.getProperty("docker.host"))) {
            return;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 2375), 1_000);
        } catch (Exception e) {
            throw new IllegalStateException("Docker Desktop TCP endpoint is closed: " + WINDOWS_DOCKER_HOST, e);
        }
    }

    /**
     * Starts PostGIS through Docker CLI on Windows where docker-java cannot talk to Docker Desktop reliably.
     */
    private static void startPostgisContainerWithDockerCli() {
        if (cliJdbcUrl != null) {
            return;
        }

        try {
            runDockerCommand("rm", "-f", CLI_CONTAINER_NAME);

            Path initScript = Path.of("src", "test", "resources", "sql", "navigation-test-data.sql")
                    .toAbsolutePath()
                    .normalize();
            String volume = initScript + ":/docker-entrypoint-initdb.d/navigation-test-data.sql:ro";

            runDockerCommand(
                    "run",
                    "-d",
                    "--name", CLI_CONTAINER_NAME,
                    "-e", "POSTGRES_DB=" + DATABASE_NAME,
                    "-e", "POSTGRES_USER=" + DATABASE_USERNAME,
                    "-e", "POSTGRES_PASSWORD=" + DATABASE_PASSWORD,
                    "-p", "5432",
                    "-v", volume,
                    "postgis/postgis:16-3.4"
            );

            waitUntilPostgresReady();
            String portOutput = runDockerCommand("port", CLI_CONTAINER_NAME, "5432/tcp");
            String port = portOutput.substring(portOutput.lastIndexOf(':') + 1).trim();
            cliJdbcUrl = "jdbc:postgresql://127.0.0.1:" + port + "/" + DATABASE_NAME;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    runDockerCommand("rm", "-f", CLI_CONTAINER_NAME);
                } catch (Exception ignored) {
                }
            }));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start PostGIS test container through Docker CLI.", e);
        }
    }

    private static void waitUntilPostgresReady() throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
        Exception lastFailure = null;

        while (System.nanoTime() < deadline) {
            try {
                runDockerCommand(
                        "exec",
                        CLI_CONTAINER_NAME,
                        "pg_isready",
                        "-U", DATABASE_USERNAME,
                        "-d", DATABASE_NAME
                );
                return;
            } catch (Exception e) {
                lastFailure = e;
                Thread.sleep(1_000);
            }
        }

        throw new IllegalStateException("PostGIS Docker CLI container did not become ready.", lastFailure);
    }

    private static String runDockerCommand(String... args) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(buildDockerCommand(args));
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        )) {
            output = reader.lines().reduce("", (left, right) -> left + right + System.lineSeparator()).trim();
        }

        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("Docker command timed out: docker " + String.join(" ", args));
        }

        if (process.exitValue() != 0 && !isAllowedDockerRmFailure(args, output)) {
            throw new IllegalStateException("Docker command failed: docker "
                    + String.join(" ", args)
                    + System.lineSeparator()
                    + output);
        }

        return output;
    }

    private static List<String> buildDockerCommand(String... args) {
        List<String> command = new java.util.ArrayList<>();
        command.add("docker");
        command.addAll(List.of(args));
        return command;
    }

    private static boolean isAllowedDockerRmFailure(String[] args, String output) {
        return args.length >= 2
                && "rm".equals(args[0])
                && "-f".equals(args[1])
                && output.contains("No such container");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
