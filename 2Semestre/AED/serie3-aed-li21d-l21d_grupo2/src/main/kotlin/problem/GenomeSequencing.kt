package problem

import graphCollections.GraphStructure
import java.io.File
import java.util.Stack

fun main(args:Array<String>){
    val k = args[0].toInt()
    val init = System.currentTimeMillis()
    val input = File(args[1]).readLines()
    val output = args[2]
    File(output).printWriter().use{
        val deBrujGraph = deBrujin(input, k)
        it.println("deBrujinGraph: ")
        for(i in deBrujGraph){
            it.println("${i.id} -> [${i.getAdjacencies().joinToString(",")}]")
        }
        val EulRoute = Eulerian(deBrujGraph, deBrujGraph.first().id)
        it.println(" ")
        it.println("EurelianRoute: ")
        it.println(EulRoute.joinToString(" -> "))
    }
    val end = System.currentTimeMillis()
    println(end-init)
}

fun deBrujin(kMers: List<String>, k: Int):GraphStructure<String>{
    val deBrujinGraph = GraphStructure<String>()
    for(i in kMers){
        deBrujinGraph.addVertex(i.substring(0, k-1))
        deBrujinGraph.addVertex(i.substring(1,k))
        deBrujinGraph.addEdge(i.substring(0, k-1), i.substring(1,k))
    }
    return deBrujinGraph
}

fun Eulerian(deBrujinGraph:GraphStructure<String>, vertex:String):List<String>{
    val knots = Stack<String>()
    val trail = mutableListOf<String>()
    knots.add(vertex)
    while(!knots.isEmpty()){
        val init = knots.peek()
        val vtx = deBrujinGraph.getVertex(init)
        if(!vtx!!.getAdjacencies().isEmpty()){
            val removed = vtx.getAdjacencies().first()
            vtx.getAdjacencies().removeFirst()
            knots.add(removed)
        }
        else{
            trail.add(knots.removeAt(knots.size-1))
        }
    }
    return trail.reversed()
}