const userNameValue = document.getElementById('userName').textContent.trim();
const svg = d3.select('#graph-container');
let simulation;


async function fetchData() {
    try {
        const response = await $.ajax({
            type: 'GET',
            url: '/user/graph'
        });
        return response;
    } catch (error) {
        console.error('Error:', error);
        throw error;
    }
}

(async () => {
    try {
        let data = await fetchData();
        console.log(data);

        // Set up simulation with center force
        simulation = d3.forceSimulation(data.nodes)
            .force('link', d3.forceLink(data.links).id(d => d.id).distance(function(d) {
                return Math.random() * (200 - 100) + 100;
            }))
            .force('charge', d3.forceManyBody().strength(-100))

        window.addEventListener('resize', updateGraphSize);
        updateGraphSize(simulation);

        const links = svg.selectAll('line')
            .data(data.links)
            .enter().append('line')
            .attr('stroke', '#999')
            .attr('stroke-width', 2);

        const nodes = svg.selectAll('circle')
            .data(data.nodes)
            .enter().append('circle')
            .attr('r', 10)
            .attr('fill', d => (d.name === userNameValue) ? 'red' : 'steelblue')
            .call(d3.drag()
                .on('start', dragstarted)
                .on('drag', dragged)
                .on('end', dragended))
            .on('click', handleNodeClick);

        simulation.on('tick', () => {
            links.attr('x1', d => d.source.x)
                .attr('y1', d => d.source.y)
                .attr('x2', d => d.target.x)
                .attr('y2', d => d.target.y);

            nodes.attr('cx', d => d.x)
                .attr('cy', d => d.y);

            labels.attr('x', d => d.x)
                .attr('y', d => d.y);
        });

        const labels = svg.selectAll('text')
            .data(data.nodes)
            .enter().append('text')
            .text(d => d.name)
            .attr('dx', 12)
            .attr('dy', 4);


    } catch (error) {
        console.error('Error:', error);
    }
})();

function updateGraphSize(simulation) {
    const screenWidth = window.innerWidth;
    svg.attr('width', screenWidth);
    simulation.force('center', d3.forceCenter(screenWidth * 0.5, 300));
}

// Drag handlers
function dragstarted(event, d) {
    if (!event.active) simulation.alphaTarget(0.3).restart();
    d.fx = d.x;
    d.fy = d.y;
}

function dragged(event, d) {
    d.fx = event.x;
    d.fy = event.y;
}

function dragended(event, d) {
    if (!event.active) simulation.alphaTarget(0);
    d.fx = null;
    d.fy = null;
}

function handleNodeClick(event, d) {
    // Implement actions to be executed when a node is clicked
    console.log('Node clicked:', d);
    // For example, you can open a modal, navigate to a different page, or perform any other action
    // Here, we're just logging the clicked node's data to the console
}
