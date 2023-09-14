package fr.gdd.raw.io;

import fr.gdd.raw.RAWConstants;
import org.apache.jena.sparql.util.Context;

import java.io.Serializable;
import java.util.Objects;

/**
 * Input objects that constitutes the configuration of a random walks query.
 * It mainly sets the stopping conditions for walking.
 */
public class RAWInput implements Serializable {

    /**
     * The number of random walks from the root to success or failure.
     * *Not* the number of scans.
     */
    public long limitRWs = Long.MAX_VALUE;

    /**
     * The timeout before stopping the walks.
     */
    public long timeout = Long.MAX_VALUE;

    /**
     * The number of random successful results.
     */
    public long limit = Long.MAX_VALUE;

    /**
     * The deadline comparatively to the time of query creation.
     */
    private long deadline = Long.MAX_VALUE;

    /* ************************************************************************ */

    public RAWInput() {}

    public RAWInput(Context context) {
        // server first, then client input
        this.limitRWs = context.isDefined(RAWConstants.limitRWs) ? context.get(RAWConstants.limitRWs) : limitRWs;
        this.setTimeout(context.isDefined(RAWConstants.timeout) ? context.get(RAWConstants.timeout) : timeout);
        if (context.isDefined(RAWConstants.input)) {
            this.limit = Math.min(this.limit,  ((RAWInput) context.get(RAWConstants.input)).limit);
            this.limitRWs = Math.min(this.limitRWs, ((RAWInput) context.get(RAWConstants.input)).limitRWs);
            this.setTimeout(Math.min(this.timeout,  ((RAWInput)context.get(RAWConstants.input)).timeout));
        }
    }

    public RAWInput(Long timeout, Long limitRWs) {
        if (Objects.nonNull(limitRWs)) {
            setLimitRWs(limitRWs);
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

    public void setLimitRWs(long limitRWs) {
        this.limitRWs = limitRWs;
    }

    public boolean deadlineReached() {
        return System.currentTimeMillis() >= deadline;
    }

    public boolean limitRWsReached(long nbRWs) {
        return nbRWs >= limitRWs;
    }

    public boolean limitReached(long nbResults) { return nbResults >= limit; }

}
