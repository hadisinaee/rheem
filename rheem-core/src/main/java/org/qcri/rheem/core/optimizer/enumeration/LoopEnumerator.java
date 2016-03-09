package org.qcri.rheem.core.optimizer.enumeration;

import org.qcri.rheem.core.optimizer.OptimizationContext;
import org.qcri.rheem.core.plan.rheemplan.LoopSubplan;
import org.qcri.rheem.core.util.OneTimeExecutable;

/**
 * Enumerator for {@link LoopSubplan}s.
 */
public class LoopEnumerator extends OneTimeExecutable {

    private final OptimizationContext.LoopContext loopContext;

    private final PlanEnumerator planEnumerator;

    private LoopEnumeration loopEnumeration;

    public LoopEnumerator(PlanEnumerator planEnumerator, OptimizationContext.LoopContext loopContext) {
        this.planEnumerator = planEnumerator;
        this.loopContext = loopContext;
    }

    public LoopEnumeration enumerate() {
        this.tryExecute();
        return this.loopEnumeration;
    }

    @Override
    protected void doExecute() {
        // Create aggregate iteration contexts.
        OptimizationContext aggregateContext = this.loopContext.createAggregateContext(0, this.loopContext.getIterationContexts().size());

        // Create the end result.
        this.loopEnumeration = new LoopEnumeration(this.loopContext.getLoop());

        // Enumerate the loop body (for now, only a single loop body).
        final PlanEnumerator loopBodyEnumerator = this.planEnumerator.forkFor(this.loopContext.getLoop().getLoopHead(), aggregateContext);
        final PlanEnumeration loopBodyEnumeration = loopBodyEnumerator.enumerate(true);
        final LoopEnumeration.IterationEnumeration iterationEnumeration = this.loopEnumeration.addIterationEnumeration(
                this.loopContext.getLoop().getNumExpectedIterations(),
                loopBodyEnumeration
        );
    }
}
