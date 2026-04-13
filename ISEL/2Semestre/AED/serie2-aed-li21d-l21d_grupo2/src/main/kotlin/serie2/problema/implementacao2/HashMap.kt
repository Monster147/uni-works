package serie2.problema.implementacao2

class HashMap<K, V> (initialCapacity: Int = 16, val loadFactor: Float = 0.75f): MutableMap<K, V> {
    class HashNode<K, V>(override val key: K, override var value: V,
                                 var next: HashNode<K, V>? = null
                                ): MutableMap.MutableEntry<K,V> {
        var hc = key.hashCode()
        override fun setValue(newValue: V): V {
            val oldValue = value
            value = newValue
            return oldValue
        }
    }
    override var size: Int=0
    override var capacity: Int=initialCapacity
    private var table: Array<HashNode<K, V>?> = arrayOfNulls(initialCapacity)

    inner class mapIterator:Iterator<MutableMap.MutableEntry<K, V>>{
        var pos = 0
        var currNode:HashNode<K, V>? = null
        var currNodeIter:HashNode<K, V>?=null
        override fun hasNext(): Boolean {
            if(currNode!=null) return true;
            while(pos < table.size){
                if(currNodeIter==null){
                    currNodeIter= table[pos++]
                }
                else{
                    currNode=currNodeIter
                    currNodeIter=currNodeIter?.next
                    return true
                }
            }
            return false
        }

        override fun next(): MutableMap.MutableEntry<K, V> {
            if(!hasNext()) throw NoSuchElementException()
            val toReturn = currNode
            currNode = null
            return toReturn!!
        }
    }

    override fun iterator(): Iterator<MutableMap.MutableEntry<K, V>> {
        return mapIterator()
    }

    private fun hash( HashCode: Int ): Int {
        return HashCode.and( 0x7fffffff ) % table.size
    }

    override fun get(key: K): V? {
        val idx = Math.abs(key.hashCode() % table.size)
        var node = table[idx]
        while(node != null){
            if(node.key == key){
                return node.value
            }
            node=node.next
        }
        return null
    }

    private fun expand() {
        val currTable = table
        table = arrayOfNulls(capacity * 2)
        capacity*=2
        for (node in currTable){
            var currNode = node
            while (currNode != null){
                val idx = hash(currNode.hc)
                //if (idx<0) idx+capacity else idx
                currNode.next = table[idx]
                table[idx] = currNode
                currNode = currNode.next
            }
        }
    }

    override fun put(key: K, value: V): V?{
        var idx = hash(key.hashCode()) //key.hashCode() % table.size
        var node = table[idx]
        while (node != null){
            if(node.key == key){
                return node.setValue(value)
            }
            node = node.next
        }
        if(++size >= loadFactor * table.size){
            expand()
            idx = hash(key.hashCode())
        }
        table[idx] = HashNode(key, value, table[idx])
        return null
    }
}