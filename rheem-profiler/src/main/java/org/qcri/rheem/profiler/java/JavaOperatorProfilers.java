package org.qcri.rheem.profiler.java;

import org.qcri.rheem.core.api.Configuration;
import org.qcri.rheem.core.function.*;
import org.qcri.rheem.core.types.DataSetType;
import org.qcri.rheem.java.operators.*;
import org.qcri.rheem.profiler.data.DataGenerators;
import org.qcri.rheem.profiler.data.UdfGenerators;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by migiwara on 11/06/17.
 */
public class JavaOperatorProfilers {

    public static JavaTextFileSourceProfiler createJavaTextFileSourceProfiler(int dataQuantaScale) {
        Configuration configuration = new Configuration();
        return new JavaTextFileSourceProfiler(
                DataGenerators.createRandomStringSupplier(dataQuantaScale+20, dataQuantaScale+40, new Random(42)),
                configuration.getStringProperty("rheem.profiler.datagen.url")
        );
    }

    public static JavaCollectionSourceProfiler createJavaCollectionSourceProfiler(int dataQuantaScale) {
        if (dataQuantaScale!=0) {
            return new JavaCollectionSourceProfiler(DataGenerators.createRandomStringSupplier(dataQuantaScale,dataQuantaScale,new Random()));
        } else {
            return new JavaCollectionSourceProfiler(DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(),0.0,new Random(),dataQuantaScale));
        }
    }

    public static JavaUnaryOperatorProfiler createJavaMapProfiler(int dataQuantaScale, int UdfComplexity) {
        if (dataQuantaScale==1){
            return createJavaMapProfiler(
                    DataGenerators.createRandomIntegerSupplier(1+dataQuantaScale,50+dataQuantaScale,new Random(42)),
                    UdfGenerators.mapIntUDF(UdfComplexity,dataQuantaScale),
                    Integer.class, Integer.class
            );
        } else {
            return new JavaUnaryOperatorProfiler(
                    () -> new JavaMapOperator<>(
                            DataSetType.createDefault(List.class),
                            DataSetType.createDefault(List.class),
                            //new TransformationDescriptor<>(i -> (int)i.stream().reduce(1, (a, b) -> (int) a * (int) b), List.class, Integer.class)
                            new TransformationDescriptor<List,List>(i -> UdfGenerators.mapIntListUDF(UdfComplexity,i.size()).apply(i),List.class, List.class)
                            //UdfGenerators.mapIntListUDF(UdfComplexity)
                    ),
                    DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(),0.0,new Random(),dataQuantaScale)
            );
        }

    }

    public static <In, Out> JavaUnaryOperatorProfiler createJavaMapProfiler(Supplier<In> dataGenerator,
                                                                        FunctionDescriptor.SerializableFunction<In, Out> udf,
                                                                        Class<In> inClass,
                                                                        Class<Out> outClass
    ) {
        return new JavaUnaryOperatorProfiler(
                () -> new JavaMapOperator<>(
                        DataSetType.createDefault(inClass),
                        DataSetType.createDefault(outClass),
                        new TransformationDescriptor<>(udf, inClass, outClass)
                ),
                dataGenerator
        );
    }

    public static JavaUnaryOperatorProfiler createJavaFlatMapProfiler(int dataQuantaScale, int UdfComplexity) {
        final Random random = new Random();
        if(dataQuantaScale==1){
            return new JavaUnaryOperatorProfiler(
                    () -> new JavaFlatMapOperator<>(
                            DataSetType.createDefault(Integer.class),
                            DataSetType.createDefault(Integer.class),
                            new FlatMapDescriptor<>(
                                    integer -> new ArrayList<>(Arrays.asList(UdfGenerators.mapIntUDF(1,dataQuantaScale).apply(integer))),
                                    Integer.class,
                                    Integer.class
                            )
                    ),
                    random::nextInt
            );
        } else {
            return new JavaUnaryOperatorProfiler(
                    () -> new JavaFlatMapOperator<>(
                            DataSetType.createDefault(List.class),
                            DataSetType.createDefault(Integer.class),
                            new FlatMapDescriptor<List,Integer>(
                                    integerList -> (ArrayList<Integer>) integerList.stream().map(el->(Integer)UdfGenerators.mapIntUDF(UdfComplexity,dataQuantaScale).apply((Integer) el))
                                            .collect(Collectors.toList()),
                                    List.class,
                                    Integer.class
                            )),
                    DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(),0.0,new Random(42),dataQuantaScale)
            );
        }

    }

    public static <In, Out> JavaUnaryOperatorProfiler createJavaFlatMapProfiler(Supplier<In> dataGenerator,
                                                                            FunctionDescriptor.SerializableFunction<In, Iterable<Out>> udf,
                                                                            Class<In> inClass,
                                                                            Class<Out> outClass) {
        return new JavaUnaryOperatorProfiler(
                () -> new JavaFlatMapOperator<>(
                        DataSetType.createDefault(inClass),
                        DataSetType.createDefault(outClass),
                        new FlatMapDescriptor<>(udf, inClass, outClass)
                ),
                dataGenerator
        );
    }

    public static JavaUnaryOperatorProfiler createJavaFilterProfiler(long dataQuantaSize, int UdfComplexity) {
        final Random random = new Random();
        if (dataQuantaSize==1){
            return new JavaUnaryOperatorProfiler(
                    () -> new JavaFilterOperator<>(
                            DataSetType.createDefault(Integer.class),
                            new PredicateDescriptor<>(UdfGenerators.filterIntUDF(UdfComplexity,(int)dataQuantaSize), Integer.class)
                    ),
                    random::nextInt
            );
        } else {
            return new JavaUnaryOperatorProfiler(
                    () -> new JavaFilterOperator<List>(
                            DataSetType.createDefault(List.class),
                            new PredicateDescriptor<List>(UdfGenerators.filterIntListUDF(UdfComplexity,(int)dataQuantaSize), List.class)
                    ),
                    DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(), 0.00, new Random(), (int) dataQuantaSize)
            );
        }
    }

    public static JavaUnaryOperatorProfiler createJavaFilterProfiler(int list, long dataQuantaSize, int UdfComplexity, int... cardinalities) {
        Random random = new Random(42);
        final List<Integer> s;
        random.nextInt();
        random.nextInt();
        int hehe= -1360544799;
        Supplier<List<Integer>> listSupplier = DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(), 0.00, new Random(42), (int) dataQuantaSize);
        s = listSupplier.get();
        return new JavaUnaryOperatorProfiler(
                () -> new JavaFilterOperator<>(
                        DataSetType.createDefault(List.class),
                        new PredicateDescriptor<>(i -> ((int)i.get(0) & 1) == 0,
                                List.class)
                ),
                DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(), 0.00, new Random(42), (int) dataQuantaSize)
        );
    }


    public static JavaUnaryOperatorProfiler createJavaReduceByProfiler(int dataQuantaScale, int UdfComplexity) {
        return createJavaReduceByProfiler(
                DataGenerators.createReservoirBasedStringSupplier(new ArrayList<>(), 0.90, new Random(42), 4 + dataQuantaScale, 20 + dataQuantaScale),
                String::new,
                (s1, s2) -> { return UdfGenerators.mapStringUDF(UdfComplexity,dataQuantaScale).apply(s1);},
                String.class,
                String.class
        );
    }

    public static <In, Key> JavaUnaryOperatorProfiler createJavaReduceByProfiler(Supplier<In> dataGenerator,
                                                                             FunctionDescriptor.SerializableFunction<In, Key> keyUdf,
                                                                             FunctionDescriptor.SerializableBinaryOperator<In> udf,
                                                                             Class<In> inOutClass,
                                                                             Class<Key> keyClass) {
        return new JavaUnaryOperatorProfiler(
                () -> new JavaReduceByOperator<>(
                        DataSetType.createDefault(inOutClass),
                        new TransformationDescriptor<>(keyUdf, inOutClass, keyClass),
                        new ReduceDescriptor<>(udf, inOutClass)
                ),
                dataGenerator
        );
    }

    public static JavaUnaryOperatorProfiler createJavaGlobalReduceProfiler(int dataQuantaScale, int UdfComplexity) {
        return createJavaGlobalReduceProfiler(
                DataGenerators.createReservoirBasedStringSupplier(new ArrayList<>(), 0.0, new Random(), 4 + dataQuantaScale, 20 + dataQuantaScale),
                (s1, s2) -> {return UdfGenerators.mapStringUDF(UdfComplexity,dataQuantaScale).apply(s1);},
                String.class
        );
    }

    public static <In> JavaUnaryOperatorProfiler createJavaGlobalReduceProfiler(Supplier<In> dataGenerator,
                                                                            FunctionDescriptor.SerializableBinaryOperator<In> udf,
                                                                            Class<In> inOutClass) {
        return new JavaUnaryOperatorProfiler(
                () -> new JavaGlobalReduceOperator<>(
                        DataSetType.createDefault(inOutClass),
                        new ReduceDescriptor<>(udf, inOutClass)
                ),
                dataGenerator
        );
    }

    public static JavaUnaryOperatorProfiler createJavaMaterializedGroupByProfiler(int dataQuantaScale, int UdfComplexity) {
        return createJavaMaterializedGroupByProfiler(
                DataGenerators.createReservoirBasedStringSupplier(new ArrayList<>(), 0.7, new Random(), 4 + dataQuantaScale, 20 + dataQuantaScale),
                s->{
                    return UdfGenerators.mapStringUDF(UdfComplexity,dataQuantaScale).apply(s);
                },
                String.class,
                String.class
        );
    }

    public static <In, Key> JavaUnaryOperatorProfiler createJavaMaterializedGroupByProfiler(Supplier<In> dataGenerator,
                                                                                        FunctionDescriptor.SerializableFunction<In, Key> keyUdf,
                                                                                        Class<In> inOutClass,
                                                                                        Class<Key> keyClass) {
        return new JavaUnaryOperatorProfiler(
                () -> new JavaMaterializedGroupByOperator<>(
                        new TransformationDescriptor<>(keyUdf, inOutClass, keyClass),
                        DataSetType.createDefault(inOutClass),
                        DataSetType.createDefaultUnchecked(Iterable.class)
                ),
                dataGenerator
        );
    }

    public static JavaUnaryOperatorProfiler createJavaCountProfiler(int dataQuantaSize) {
        if (dataQuantaSize==1){
            return new JavaUnaryOperatorProfiler(
                    () -> new JavaCountOperator<>(DataSetType.createDefault(Integer.class)),
                    DataGenerators.createRandomIntegerSupplier(new Random())
            );
        } else {
            return new JavaUnaryOperatorProfiler(
                    () -> new JavaCountOperator<>(DataSetType.createDefault(List.class)),
                    DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(), 0.00, new Random(), (int) dataQuantaSize)
            );
        }
    }

    public static <T> JavaUnaryOperatorProfiler createJavaCountProfiler(Supplier<T> dataGenerator,
                                                                    Class<T> inClass) {
        return new JavaUnaryOperatorProfiler(
                () -> new JavaCountOperator<>(DataSetType.createDefault(inClass)),
                dataGenerator
        );
    }

    public static JavaUnaryOperatorProfiler createJavaDistinctProfiler(int dataQuantaScale) {
        return createJavaDistinctProfiler(
                DataGenerators.createReservoirBasedStringSupplier(new ArrayList<>(), 0.5, new Random(), 4 + dataQuantaScale, 20 + dataQuantaScale),
                String.class
        );
    }

    public static <T> JavaUnaryOperatorProfiler createJavaDistinctProfiler(Supplier<?> dataGenerator, Class<T> inClass) {
        return new JavaUnaryOperatorProfiler(
                () -> new JavaDistinctOperator<>(DataSetType.createDefault(inClass)),
                dataGenerator
        );
    }

    public static JavaUnaryOperatorProfiler createJavaSortProfiler(int dataQuantaScale,int UdfComplexity) {
        return new JavaUnaryOperatorProfiler(() -> new JavaSortOperator<>(new TransformationDescriptor<>(in->{
            return UdfGenerators.mapStringUDF(UdfComplexity,dataQuantaScale).apply((String)in);
        }, String.class, String.class),
                DataSetType.createDefault(String.class)),
                DataGenerators.createReservoirBasedStringSupplier(new ArrayList<>(), 0.0, new Random(), 4 + dataQuantaScale, 20 + dataQuantaScale));
    }

    public static <T> JavaUnaryOperatorProfiler createJavaSortProfiler(Supplier<?> dataGenerator, Class<T> inClass) {
        return new JavaUnaryOperatorProfiler(() -> new JavaSortOperator<>(new TransformationDescriptor<>(in->in, inClass, inClass),
                DataSetType.createDefault(inClass)), dataGenerator);
    }

    public static JavaBinaryOperatorProfiler createJavaJoinProfiler(int dataQuantaScale, int UdfComplexity) {
        final List<String> stringReservoir = new ArrayList<>();
        final double reuseProbability = 1.0;
        final Random random = new Random();
        final int minLen = 4, maxLen = 6;
        Supplier<String> reservoirStringSupplier = DataGenerators.createReservoirBasedStringSupplier(stringReservoir, reuseProbability, random, minLen + dataQuantaScale, maxLen + dataQuantaScale);
        return new JavaBinaryOperatorProfiler(
                () -> new JavaJoinOperator<>(
                        DataSetType.createDefault(String.class),
                        DataSetType.createDefault(String.class),
                        new TransformationDescriptor<>(
                                s -> {
                                    return UdfGenerators.mapStringUDF(UdfComplexity,dataQuantaScale).apply(s);
                                },
                                String.class,
                                String.class
                        ),
                        new TransformationDescriptor<>(
                                String::new,
                                String.class,
                                String.class
                        )
                ),
                reservoirStringSupplier,
                reservoirStringSupplier
        );
    }

    /**
     * Creates a {@link JavaBinaryOperatorProfiler} for the {@link JavaCartesianOperator} with {@link Integer} data quanta.
     */
    public static JavaBinaryOperatorProfiler createJavaCartesianProfiler(int dataQuantaSize) {
        if (dataQuantaSize==1){
            return new JavaBinaryOperatorProfiler(
                    () -> new JavaCartesianOperator<>(
                            DataSetType.createDefault(Integer.class),
                            DataSetType.createDefault(Integer.class)
                    ),
                    DataGenerators.createRandomIntegerSupplier(new Random()),
                    DataGenerators.createRandomIntegerSupplier(new Random())
            );
        } else {
            return new JavaBinaryOperatorProfiler(
                    () -> new JavaCartesianOperator<>(
                            DataSetType.createDefault(List.class),
                            DataSetType.createDefault(List.class)
                    ),
                    DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(), 0.00, new Random(), (int) dataQuantaSize),
                    DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(), 0.00, new Random(), (int) dataQuantaSize)
            );
        }

    }

    public static JavaBinaryOperatorProfiler createJavaUnionProfiler(int dataQuantaSize) {
        final List<String> stringReservoir = new ArrayList<>();
        final double reuseProbability = 0.0;
        final Random random = new Random();
        Supplier<String> reservoirStringSupplier = DataGenerators.createReservoirBasedStringSupplier(stringReservoir, reuseProbability, random, 4 + dataQuantaSize, 6 + dataQuantaSize);
        return new JavaBinaryOperatorProfiler(
                () -> new JavaUnionAllOperator<>(DataSetType.createDefault(String.class)),
                reservoirStringSupplier,
                reservoirStringSupplier
        );
    }

    public static JavaSinkProfiler createJavaLocalCallbackSinkProfiler(int dataQuantaSize) {
        if (dataQuantaSize == 1){
            return new JavaSinkProfiler(
                    () -> new JavaLocalCallbackSink<>(obj -> {
                    }, DataSetType.createDefault(Integer.class)),
                    DataGenerators.createRandomIntegerSupplier(new Random())
            );
        } else {
            return new JavaSinkProfiler(
                    () -> new JavaLocalCallbackSink<>(obj -> {
                    }, DataSetType.createDefault(List.class)),
                    DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(), 0.00, new Random(), (int) dataQuantaSize)
            );
        }

    }

    public static <T> JavaSinkProfiler createCollectingJavaLocalCallbackSinkProfiler( int dataQuantaSize) {
        Collection<T> collector = new LinkedList<>();
        if (dataQuantaSize == 1){
            return new JavaSinkProfiler(
                    () -> new JavaLocalCallbackSink<>(collector::add, DataSetType.createDefault(Integer.class)),
                    DataGenerators.createRandomIntegerSupplier(new Random())
            );
        } else {
            return new JavaSinkProfiler(
                    () -> new JavaLocalCallbackSink<>(collector::add, DataSetType.createDefault(List.class)),
                    DataGenerators.createReservoirBasedIntegerListSupplier(new ArrayList<List<Integer>>(), 0.00, new Random(), (int) dataQuantaSize)
            );
        }

    }
}
