const state = {
    mode: 'start',
    graph: null,
    nodeById: new Map(),
    edgeByKey: new Map(),
    startSelection: null,
    destinationSelection: null,
    sessionId: null,
    session: null,
    pollTimer: null
};

const map = L.map('map', {
    zoomControl: true,
    preferCanvas: true
}).setView([37.0, 127.0], 17);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap contributors',
    maxZoom: 21
}).addTo(map);

const graphLayer = L.layerGroup().addTo(map);
const routeLayer = L.layerGroup().addTo(map);
const selectionLayer = L.layerGroup().addTo(map);

const modeStartButton = document.getElementById('modeStart');
const modeDestinationButton = document.getElementById('modeDestination');
const clearButton = document.getElementById('clearButton');
const routeButton = document.getElementById('routeButton');

function setMode(mode) {
    state.mode = mode;
    modeStartButton.classList.toggle('active', mode === 'start');
    modeDestinationButton.classList.toggle('active', mode === 'destination');
    document.getElementById('modeLabel').textContent = mode;
}

function formatLatLng(point) {
    return `${point.latitude.toFixed(6)}, ${point.longitude.toFixed(6)}`;
}

function toLatLng(point) {
    return [point.latitude, point.longitude];
}

function clearLayers() {
    graphLayer.clearLayers();
    routeLayer.clearLayers();
    selectionLayer.clearLayers();
}

function makeEdgeKey(fromNodeId, toNodeId) {
    return `${fromNodeId}->${toNodeId}`;
}

function renderGraph() {
    if (!state.graph) {
        return;
    }

    graphLayer.clearLayers();
    routeLayer.clearLayers();

    const routePath = state.session?.result?.path || [];

    for (const edge of state.graph.edges) {
        const latlngs = edge.geometry.map(point => toLatLng(point));
        const color =
            edge.highway === 'steps' ? '#ef4444' :
            edge.highway === 'ramp' ? '#22c55e' :
            '#7c3aed';

        const polyline = L.polyline(latlngs, {
            color,
            weight: 2,
            opacity: 0.7
        }).addTo(graphLayer);

        polyline.bindTooltip(`edge ${edge.fromNodeId} -> ${edge.toNodeId}<br>${edge.highway}<br>cost=${edge.cost.toFixed(2)}`, {
            sticky: true
        });
    }

    for (const node of state.graph.nodes) {
        const color =
            node.nodeType === 'ENTRANCE' ? '#f97316' :
            '#000000';

        const marker = L.circleMarker([node.latitude, node.longitude], {
            radius: node.nodeType === 'ENTRANCE' ? 6 : 4,
            color,
            fillColor: color,
            fillOpacity: 0.9,
            weight: 1
        }).addTo(graphLayer);

        const label = node.description ? `${node.nodeType} / ${node.description}` : `${node.nodeType} / ${node.id}`;
        marker.bindTooltip(label, {sticky: true});
    }

    if (routePath.length > 1) {
        L.polyline(routePath.map(point => toLatLng(point)), {
            color: '#f59e0b',
            weight: 5,
            opacity: 0.95
        }).addTo(routeLayer);
    }
}

function renderSelections() {
    selectionLayer.clearLayers();

    if (state.startSelection) {
        const input = state.startSelection.inputPoint;
        const projected = state.startSelection.projectedPoint;
        L.circleMarker(toLatLng(input), {radius: 7, color: '#22c55e', fillColor: '#22c55e', fillOpacity: 1})
            .bindPopup('Start point')
            .addTo(selectionLayer);
        L.circleMarker(toLatLng(projected), {radius: 7, color: '#10b981', fillColor: '#10b981', fillOpacity: 1})
            .bindPopup('Projected start')
            .addTo(selectionLayer);
        L.polyline([toLatLng(input), toLatLng(projected)], {color: '#22c55e', weight: 3, dashArray: '6 6'})
            .addTo(selectionLayer);
    }

    if (state.destinationSelection) {
        const input = state.destinationSelection.inputPoint;
        const projected = state.destinationSelection.projectedPoint;
        L.circleMarker(toLatLng(input), {radius: 7, color: '#ef4444', fillColor: '#ef4444', fillOpacity: 1})
            .bindPopup('Destination point')
            .addTo(selectionLayer);
        L.circleMarker(toLatLng(projected), {radius: 7, color: '#f97316', fillColor: '#f97316', fillOpacity: 1})
            .bindPopup('Projected destination')
            .addTo(selectionLayer);
        L.polyline([toLatLng(input), toLatLng(projected)], {color: '#ef4444', weight: 3, dashArray: '6 6'})
            .addTo(selectionLayer);
    }
}

async function loadGraph() {
    const response = await fetch('/api/navigation/graph-map/geo/graph');
    state.graph = await response.json();
    state.nodeById = new Map(state.graph.nodes.map(node => [node.id, node]));
    state.edgeByKey = new Map();
    for (const edge of state.graph.edges) {
        state.edgeByKey.set(makeEdgeKey(edge.fromNodeId, edge.toNodeId), edge);
    }

    renderGraph();

    const bounds = L.latLngBounds([]);
    for (const node of state.graph.nodes) {
        bounds.extend([node.latitude, node.longitude]);
    }
    if (bounds.isValid()) {
        map.fitBounds(bounds.pad(0.12));
    }

    document.getElementById('connectionStatus').textContent = `Loaded ${state.graph.nodes.length} nodes / ${state.graph.edges.length} edges`;
}

async function projectPoint(point) {
    const response = await fetch('/api/navigation/graph-map/geo/projections', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(point)
    });
    return await response.json();
}

async function selectPoint(latlng) {
    const projection = await projectPoint({
        longitude: latlng.lng,
        latitude: latlng.lat,
        altitude: 0.0
    });

    const payload = {
        inputPoint: {
            longitude: latlng.lng,
            latitude: latlng.lat,
            altitude: 0.0
        },
        projectedPoint: projection.projectedPoint,
        edgeFromNodeId: projection.edgeFromNodeId,
        edgeToNodeId: projection.edgeToNodeId,
        distanceToEdge: projection.distanceToEdge,
        accessCostToFromNode: projection.accessCostToFromNode,
        accessCostToToNode: projection.accessCostToToNode
    };

    if (state.mode === 'start') {
        state.startSelection = payload;
        document.getElementById('startLabel').textContent = formatLatLng(payload.inputPoint);
    } else {
        state.destinationSelection = payload;
        document.getElementById('destinationLabel').textContent = formatLatLng(payload.inputPoint);
    }

    document.getElementById('projectionText').textContent = JSON.stringify(payload, null, 2);
    renderSelections();
}

async function startRoute() {
    if (!state.startSelection || !state.destinationSelection) {
        alert('Start and destination must be selected first.');
        return;
    }

    const response = await fetch('/api/navigation/graph-map/geo/routes', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            startPoint: state.startSelection.inputPoint,
            destinationPoint: state.destinationSelection.inputPoint
        })
    });
    state.session = await response.json();
    state.sessionId = state.session.sessionId;

    document.getElementById('routeStatus').textContent = state.session.status;
    document.getElementById('visitedCount').textContent = '-';
    document.getElementById('frontierSize').textContent = '-';
    document.getElementById('currentNode').textContent = '-';
    document.getElementById('routeResult').textContent = state.session.result
        ? JSON.stringify(state.session.result, null, 2)
        : state.session.message ?? '-';

    if (state.pollTimer) {
        clearInterval(state.pollTimer);
        state.pollTimer = null;
    }
    renderGraph();
}

async function pollSession() {
    if (!state.sessionId) return;
    const response = await fetch(`/api/navigation/graph-map/geo/routes/${state.sessionId}`);
    state.session = await response.json();

    document.getElementById('routeStatus').textContent = state.session.status;
    document.getElementById('visitedCount').textContent = '-';
    document.getElementById('frontierSize').textContent = '-';
    document.getElementById('currentNode').textContent = '-';
    document.getElementById('routeResult').textContent = state.session.result
        ? JSON.stringify(state.session.result, null, 2)
        : state.session.message;

    renderGraph();

    if (state.session.status !== 'RUNNING' && state.pollTimer) {
        clearInterval(state.pollTimer);
        state.pollTimer = null;
    }
}

function clearSelections() {
    state.startSelection = null;
    state.destinationSelection = null;
    state.session = null;
    state.sessionId = null;

    document.getElementById('startLabel').textContent = '-';
    document.getElementById('destinationLabel').textContent = '-';
    document.getElementById('projectionText').textContent = '-';
    document.getElementById('routeStatus').textContent = 'idle';
    document.getElementById('visitedCount').textContent = '0';
    document.getElementById('frontierSize').textContent = '0';
    document.getElementById('currentNode').textContent = '-';
    document.getElementById('routeResult').textContent = '-';

    renderGraph();
    renderSelections();
}

map.on('click', async event => {
    await selectPoint(event.latlng);
});

modeStartButton.addEventListener('click', () => setMode('start'));
modeDestinationButton.addEventListener('click', () => setMode('destination'));
clearButton.addEventListener('click', clearSelections);
routeButton.addEventListener('click', startRoute);
window.addEventListener('resize', () => map.invalidateSize());

setMode('start');
loadGraph().catch(error => {
    document.getElementById('connectionStatus').textContent = error.message;
});
