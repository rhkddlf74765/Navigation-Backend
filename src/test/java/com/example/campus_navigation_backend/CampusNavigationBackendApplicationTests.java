package com.example.campus_navigation_backend;

import com.example.campus_navigation_backend.support.PostgisTestContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot 애플리케이션 컨텍스트가 테스트 DB 설정으로 정상 기동되는지 검증하는 기본 통합 테스트이다.
 */
@SpringBootTest
class CampusNavigationBackendApplicationTests extends PostgisTestContainerSupport {

	/**
	 * 전체 Spring context가 로딩되고 필수 Bean 초기화가 실패하지 않는지 검증한다.
	 * <p>중점 검증 대상은 Testcontainers 기반 datasource 설정과 애플리케이션 초기화 이벤트의 정상 수행 여부이다.
	 */
	@Test
	void contextLoads() {
	}

}
