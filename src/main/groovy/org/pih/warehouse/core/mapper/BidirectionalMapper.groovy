package org.pih.warehouse.core.mapper

/**
 * Converter between two different source objects that supports converting in both directions (A -> B and B -> A).
 */
trait BidirectionalMapper<Source, OtherSource> implements Mapper<Source, OtherSource> {

    /**
     * Converts an instance of the source object into a new instance of the target object.
     *
     * @param source The object to be converted from.
     * @param config The configuration to use when performing the mapping.
     * @return A new instance of the target object.
     */
    abstract Source map(OtherSource source, MapperConfig config=null)

    /**
     * Converts a collection of instances of the source object into a List of instances of the target object.
     *
     * @param sources The objects to be converted from.
     * @param config The configuration to use when performing the mapping.
     * @return A new List of instances of the target object.
     */
    List<Source> mapCollection(Collection<OtherSource> sources, MapperConfig config=null) {
        if (sources == null) {
            return null
        }

        List<Source> mappedList = []
        for (source in sources) {
            mappedList.add(map(source, config))
        }
        return mappedList
    }
}
