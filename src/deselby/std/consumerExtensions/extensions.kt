package deselby.std.consumerExtensions


inline fun<T> MutableCollection<T>.asConsumer(): (T) -> Unit {
    return { this.add(it) }
}

inline fun<K,V> MutableMap<K,V>.asBiconsumer(): (K,V) -> Unit {
    return { key, value -> this[key] = value }
}

inline fun<K,V> MutableMap<K,V>.asPairConsumer(): (Pair<K,V>) -> Unit {
    return { (key, value) -> this[key] = value }
}

inline fun<K,V> MutableMap<K,V>.asEntryConsumer(): (Map.Entry<K,V>) -> Unit {
    return { (key, value) -> this[key] = value }
}

inline fun<A,B> ((B) -> Unit).map(crossinline transform : (A) -> B) : (A) -> Unit {
    return {a -> this(transform(a)) }
}


inline fun<A,B,X> ((X) -> Unit).map(crossinline transform : (A,B) -> X) : (A,B) -> Unit {
    return {a, b -> this(transform(a,b)) }
}


inline fun<A,B,X,Y> ((X,Y) -> Unit).map(crossinline transform : (A,B) -> Pair<X,Y>) : (A,B) -> Unit {
    return { a, b ->
        val (x,y) = transform(a, b)
        this(x,y) }
}

inline fun<A,X,Y> ((X,Y) -> Unit).map(crossinline transform : (A) -> Pair<X,Y>) : (A) -> Unit {
    return { a ->
        val (x,y) = transform(a)
        this(x,y) }
}

inline fun<A> ((A) -> Unit).andThen(crossinline after : (A) -> Unit) : (A) -> Unit {
    return {a ->
        this(a)
        after(a)
    }
}
