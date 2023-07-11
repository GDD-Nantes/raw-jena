package fr.gdd.sage.io;

import fr.gdd.sage.RAWConstants;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.util.Context;

import java.io.Serializable;
import java.util.Objects;

/**
 * Input objects that constitutes the configuration of a random walks query.
 */
public class RAWInput implements Serializable {

    public long timeout = Long.MAX_VALUE;
    public long limit = Long.MAX_VALUE;
    public long deadline = Long.MAX_VALUE;

    public RAWInput() {}

    public RAWInput(Context context) {
        // server first, then client input
        this.limit = context.isDefined(RAWConstants.limit) ? context.get(RAWConstants.limit) : limit;
        this.setTimeout(context.isDefined(RAWConstants.timeout) ? context.get(RAWConstants.timeout) : timeout);
        if (context.isDefined(RAWConstants.input)) {
            this.limit = Math.min(this.limit, ((RAWInput) context.get(RAWConstants.input)).limit);
            this.setTimeout(Math.min(this.timeout,  ((RAWInput)context.get(RAWConstants.input)).timeout));
        }
    }

    public RAWInput(Long timeout, Long limit) {
        if (Objects.nonNull(limit)) {
            setLimit(limit);
        }
        if (Objects.nonNull(timeout)) {
            setTimeout(timeout);
        }
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
        deadline = System.currentTimeMillis() + this.timeout;
        deadline = deadline > 0 ? deadline : Long.MAX_VALUE; // overflowed
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public boolean deadlineReached() {
        return System.currentTimeMillis() >= deadline;
    }

    public boolean limitReached(long results) {
        return results >= limit;
    }

}
