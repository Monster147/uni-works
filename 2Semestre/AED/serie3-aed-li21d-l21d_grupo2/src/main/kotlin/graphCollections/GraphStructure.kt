package graphCollections

class GraphStructure<I> : Graph<I> {
    private val vertex: MutableMap<I, graphVertex<I>> = mutableMapOf()

    override val size: Int
        get() = vertex.size

    override fun addVertex(id: I): Boolean {
        if (vertex.containsKey(id)) return false //if (id in vertex) return false
        vertex[id] = graphVertex(id)
        return true
    }

    override fun addEdge(id: I, idAdj: I): I? {
        val vertex = vertex.get(id)
        if (vertex == null) return null
        vertex.addAdjacency(idAdj)
        return idAdj
    }

    override fun getVertex(id: I): Graph.Vertex<I>? = vertex[id]


    override fun iterator(): Iterator<Graph.Vertex<I>> = graphIterator()

     private class graphVertex<I>(override val id: I) : Graph.Vertex<I> {

        private val adj: MutableList<I> = mutableListOf()

        override fun getAdjacencies(): MutableList<I> = adj

        fun addAdjacency(adjency: I) = adj.add(adjency)

    }

    private inner class graphIterator : Iterator<Graph.Vertex<I>> {
        private var idx = 0
        private val nums = vertex.values.toTypedArray()

        override fun hasNext(): Boolean = idx < nums.size

        override fun next(): Graph.Vertex<I> {
            if (!hasNext()) throw NoSuchElementException()
            return nums[idx++]
        }
    }
}




