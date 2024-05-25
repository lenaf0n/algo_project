const svg = d3.select('#graph-container');
let simulation;

async function fetchData() {
    const userId = $('#userDiv').attr('data-userId');
    try {
        const response = await $.ajax({
            type: 'GET',
            url: '/user/interest-graph/' + userId
        });
        if (response != null) {
            return response;
        }
        alert("This user is private!");
    } catch (error) {
        console.error('Error:', error);
        throw error;
    }
}

(async () => {
    try {
        let data = await fetchData();
        console.log(data);

        simulation = d3.forceSimulation(data.nodes)
            .force('link', d3.forceLink(data.links).id(d => d.id).distance(function(d) {
                return Math.random() * (300 - 100) + 100;
            }))
            .force('charge', d3.forceManyBody().strength(-100))
            .force('center', d3.forceCenter(400, 350));

        const links = svg.selectAll('line')
            .data(data.links)
            .enter().append('line')
            .attr('stroke', '#999')
            .attr('stroke-width', 2);

        const nodes = svg.selectAll('circle')
            .data(data.nodes)
            .enter().append('circle')
            .attr('r', 10)
            .attr('fill', d => (d.type === 'USER') ? 'steelblue' : 'rgb(250, 218, 94)')
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

        nodes.on('mouseover', function(event, d) {
            d3.select(this)
                .attr('r', 12);
            svg.style('cursor', 'pointer');
        })
            .on('mouseout', function(event, d) {
                d3.select(this)
                    .attr('r', 10);
                svg.style('cursor', 'default');
            });


    } catch (error) {
        console.error('Error:', error);
    }
})();

function dragstarted(event, d) {
    if (!event.active) simulation.alphaTarget(0.3).restart();
    if (d.name === userNameValue) {
        // For the red node, fix its position
        d.fx = d.x;
        d.fy = d.y;
    }
}

function dragged(event, d) {
    if (d.name !== userNameValue) {
        d.fx = event.x;
        d.fy = event.y;
    }
}

function dragended(event, d) {
    if (!event.active) simulation.alphaTarget(0);
    d.fx = null;
    d.fy = null;
}


function handleNodeClick(event, d) {
    console.log('Node clicked:', d);

    if (d.type === 'INTEREST') {
        let cleanedStr = d.id.replace('interest', '');
        console.log(cleanedStr)
        window.location.href = `/interest-page/${cleanedStr}`
    }
}
