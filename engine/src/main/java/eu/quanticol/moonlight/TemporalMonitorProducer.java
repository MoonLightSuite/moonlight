package eu.quanticol.moonlight;

import eu.quanticol.moonlight.core.formula.Interval;
import eu.quanticol.moonlight.core.signal.SignalDomain;
import eu.quanticol.moonlight.offline.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.io.MoonLightRecord;
import eu.quanticol.moonlight.offline.signal.RecordHandler;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface TemporalMonitorProducer {

    <S> TemporalMonitor<MoonLightRecord,S> apply(SignalDomain<S> domain, MoonLightRecord args);

    static TemporalMonitorProducer produceImplication(TemporalMonitorProducer left, TemporalMonitorProducer right) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.impliesMonitor(left.apply(domain,args),domain,right.apply(domain,args));
            }
        };
    }

    static TemporalMonitorProducer produceAtomic(BiFunction<MoonLightRecord, MoonLightRecord, Double> atomic) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.atomicMonitor(s -> domain.valueOf(atomic.apply(args,s)));
            }
        };
    }

    static TemporalMonitorProducer produceAtomic(BiFunction<MoonLightRecord,MoonLightRecord,Double> left, String op, BiFunction<MoonLightRecord,MoonLightRecord,Double> right) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.atomicMonitor(s -> SignalDomain.getOperator(domain,op).apply(left.apply(args,s), right.apply(args,s)));
            }
        };
    }

    static TemporalMonitorProducer produceAnd(TemporalMonitorProducer left, TemporalMonitorProducer right) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.andMonitor(left.apply(domain,args),domain,right.apply(domain,args));
            }
        };
    }

    static TemporalMonitorProducer produceOr(TemporalMonitorProducer left, TemporalMonitorProducer right) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.orMonitor(left.apply(domain,args),domain,right.apply(domain,args));
            }
        };
    }

    static TemporalMonitorProducer produceNegation(TemporalMonitorProducer arg) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.notMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static TemporalMonitorProducer produceFalse() {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.atomicMonitor(v -> domain.min());
            }
        };
    }

    static TemporalMonitorProducer produceTrue() {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.atomicMonitor(v -> domain.max());
            }
        };
    }

    static TemporalMonitorProducer produceOnce(TemporalMonitorProducer arg) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.onceMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static TemporalMonitorProducer produceHistorically(TemporalMonitorProducer arg) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.historicallyMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static TemporalMonitorProducer produceEventually(TemporalMonitorProducer arg) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.eventuallyMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static TemporalMonitorProducer produceGlobally(TemporalMonitorProducer arg) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.globallyMonitor(arg.apply(domain,args),domain);
            }
        };
    }

    static TemporalMonitorProducer produceOnce(TemporalMonitorProducer arg, Function<MoonLightRecord, Interval> interval) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.onceMonitor(arg.apply(domain,args),domain,interval.apply(args));
            }
        };
    }

    static TemporalMonitorProducer produceHistorically(TemporalMonitorProducer arg, Function<MoonLightRecord, Interval> interval) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.historicallyMonitor(arg.apply(domain,args),domain,interval.apply(args));
            }
        };
    }

    static TemporalMonitorProducer produceEventually(TemporalMonitorProducer arg, Function<MoonLightRecord, Interval> interval) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.eventuallyMonitor(arg.apply(domain,args),domain,interval.apply(args));
            }
        };
    }

    static TemporalMonitorProducer produceGlobally(TemporalMonitorProducer arg, Function<MoonLightRecord, Interval> interval) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.globallyMonitor(arg.apply(domain,args),domain,interval.apply(args));
            }
        };
    }

    static TemporalMonitorProducer produceUntil(TemporalMonitorProducer left, TemporalMonitorProducer right) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.untilMonitor(left.apply(domain,args), right.apply(domain,args), domain);
            }
        };
    }

    static TemporalMonitorProducer produceUntil(TemporalMonitorProducer left, Function<MoonLightRecord, Interval> interval, TemporalMonitorProducer right) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.untilMonitor(left.apply(domain,args), interval.apply(args), right.apply(domain,args), domain);
            }
        };
    }

    static TemporalMonitorProducer produceSince(TemporalMonitorProducer left, TemporalMonitorProducer right) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.sinceMonitor(left.apply(domain,args), right.apply(domain,args), domain);
            }
        };
    }

    static TemporalMonitorProducer produceSince(TemporalMonitorProducer left, Function<MoonLightRecord, Interval> interval, TemporalMonitorProducer right) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return TemporalMonitor.sinceMonitor(left.apply(domain,args), interval.apply(args), right.apply(domain,args), domain);
            }
        };
    }

    static TemporalMonitorProducer produceCall(TemporalMonitorProducer temporalMonitorProducer, RecordHandler callee, List<Function<MoonLightRecord, Double>> functionArgument) {
        return new TemporalMonitorProducer() {
            @Override
            public <S> TemporalMonitor<MoonLightRecord, S> apply(SignalDomain<S> domain, MoonLightRecord args) {
                return temporalMonitorProducer.apply(domain,callee.fromDoubleArray(functionArgument.stream().mapToDouble(f -> f.apply(args)).toArray()));
            }
        };
    }


}
