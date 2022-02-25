package eu.quanticol.moonlight;

import eu.quanticol.moonlight.core.space.DistanceStructure;
import eu.quanticol.moonlight.domain.Interval;
import eu.quanticol.moonlight.domain.SignalDomain;
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor;
import eu.quanticol.moonlight.io.MoonLightRecord;
import eu.quanticol.moonlight.signal.RecordHandler;
import eu.quanticol.moonlight.core.space.SpatialModel;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface SpatialTemporalMonitorProducer {

    <S> SpatialTemporalMonitor<MoonLightRecord,MoonLightRecord,S> apply(SignalDomain<S> domain, MoonLightRecord args);

    static SpatialTemporalMonitorProducer produceImplication(SpatialTemporalMonitorProducer left, SpatialTemporalMonitorProducer right) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.impliesMonitor(left.apply(domain,args),domain,right.apply(domain,args));
            }
        };
    }

    static SpatialTemporalMonitorProducer produceAtomic(BiFunction<MoonLightRecord, MoonLightRecord, Double> atomic) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.atomicMonitor(s -> domain.valueOf(atomic.apply(args,s)));
            }
        };
    }

    static SpatialTemporalMonitorProducer produceAtomic(BiFunction<MoonLightRecord,MoonLightRecord,Double> left, String op, BiFunction<MoonLightRecord,MoonLightRecord,Double> right) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.atomicMonitor(s -> SignalDomain.getOperator(domain,op).apply(left.apply(args,s), right.apply(args,s)));
            }
        };
    }

    static SpatialTemporalMonitorProducer produceAnd(SpatialTemporalMonitorProducer left, SpatialTemporalMonitorProducer right) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.andMonitor(left.apply(domain,args),domain,right.apply(domain,args));
            }
        };
    }

    static SpatialTemporalMonitorProducer produceOr(SpatialTemporalMonitorProducer left, SpatialTemporalMonitorProducer right) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.orMonitor(left.apply(domain,args),domain,right.apply(domain,args));
            }
        };
    }

    static SpatialTemporalMonitorProducer produceNegation(SpatialTemporalMonitorProducer arg) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.notMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceFalse() {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.atomicMonitor(v -> domain.min());
            }
        };
    }

    static SpatialTemporalMonitorProducer produceTrue() {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.atomicMonitor(v -> domain.max());
            }
        };
    }

    static SpatialTemporalMonitorProducer produceOnce(SpatialTemporalMonitorProducer arg) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.onceMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceHistorically(SpatialTemporalMonitorProducer arg) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.historicallyMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceEventually(SpatialTemporalMonitorProducer arg) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.eventuallyMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceGlobally(SpatialTemporalMonitorProducer arg) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.globallyMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceOnce(SpatialTemporalMonitorProducer arg, Function<MoonLightRecord, Interval> interval) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.onceMonitor(arg.apply(domain,args),interval.apply(args), domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceHistorically(SpatialTemporalMonitorProducer arg, Function<MoonLightRecord, Interval> interval) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.historicallyMonitor(arg.apply(domain,args),interval.apply(args),domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceEventually(SpatialTemporalMonitorProducer arg, Function<MoonLightRecord, Interval> interval) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.eventuallyMonitor(arg.apply(domain,args),interval.apply(args),domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceGlobally(SpatialTemporalMonitorProducer arg, Function<MoonLightRecord, Interval> interval) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.globallyMonitor(arg.apply(domain,args),interval.apply(args),domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceUntil(SpatialTemporalMonitorProducer left, SpatialTemporalMonitorProducer right) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.untilMonitor(left.apply(domain,args), right.apply(domain,args), domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceUntil(SpatialTemporalMonitorProducer left, Function<MoonLightRecord, Interval> interval, SpatialTemporalMonitorProducer right) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.untilMonitor(left.apply(domain,args), interval.apply(args), right.apply(domain,args), domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceSince(SpatialTemporalMonitorProducer left, SpatialTemporalMonitorProducer right) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.sinceMonitor(left.apply(domain,args), right.apply(domain,args), domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceSince(SpatialTemporalMonitorProducer left, Function<MoonLightRecord, Interval> interval, SpatialTemporalMonitorProducer right) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.sinceMonitor(left.apply(domain,args), interval.apply(args), right.apply(domain,args), domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceReach(SpatialTemporalMonitorProducer left, Function<MoonLightRecord,Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord, ?>>> distance, SpatialTemporalMonitorProducer right) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.reachMonitor(left.apply(domain,args), distance.apply(args), right.apply(domain,args), domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceEscape(SpatialTemporalMonitorProducer arg, Function<MoonLightRecord,Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord, ?>>> distance) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.escapeMonitor(arg.apply(domain,args), distance.apply(args), domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceSomewhere(SpatialTemporalMonitorProducer arg, Function<MoonLightRecord,Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord, ?>>> distance) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.somewhereMonitor(arg.apply(domain,args), distance.apply(args), domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceEverywhere(SpatialTemporalMonitorProducer arg, Function<MoonLightRecord,Function<SpatialModel<MoonLightRecord>, DistanceStructure<MoonLightRecord, ?>>> distance) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return SpatialTemporalMonitor.everywhereMonitor(arg.apply(domain,args), distance.apply(args), domain);
            }
        };
    }

    static SpatialTemporalMonitorProducer produceCall(SpatialTemporalMonitorProducer spatialTemporalMonitorProducer, RecordHandler callee, List<Function<MoonLightRecord, Double>> functionArgument) {
        return new SpatialTemporalMonitorProducer() {
            @Override
            public <S> SpatialTemporalMonitor<MoonLightRecord, MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return spatialTemporalMonitorProducer.apply(domain,callee.fromDoubleArray(functionArgument.stream().mapToDouble(f -> f.apply(args)).toArray()));
            }
        };
    }


}
