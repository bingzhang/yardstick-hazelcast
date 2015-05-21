/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstickframework.hazelcast;

import com.hazelcast.core.*;
import com.hazelcast.transaction.*;

import java.util.*;

import static com.hazelcast.transaction.TransactionOptions.TransactionType.*;
import static org.yardstickframework.BenchmarkUtils.*;

/**
 * Hazelcast benchmark that performs transactional put and get operations.
 */
public class HazelcastPutGetTxPessimisticBenchmark extends HazelcastAbstractBenchmark {
    /** */
    public HazelcastPutGetTxPessimisticBenchmark() {
        super("map");
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        TransactionContext tCtx = null;

        try {

            int key = nextRandom(0, args.range() / 2);

            // Repeatable read isolation level is always used.
            TransactionOptions txOpts = new TransactionOptions().setTransactionType(TWO_PHASE);

            tCtx = hazelcast().newTransactionContext(txOpts);

            tCtx.beginTransaction();

            TransactionalMap<Object, Object> txMap = tCtx.getMap("map");


            Object val = txMap.getForUpdate(key);

            if (val != null)
                key = nextRandom(args.range() / 2, args.range());

            txMap.put(key, new SampleValue(key));

            tCtx.commitTransaction();
        }
        catch (Exception e) {
            println(cfg, "Transaction will be rollback.");

            e.printStackTrace(cfg.error());

            try {
                tCtx.rollbackTransaction();
            }
            catch (Exception e1) {
                println(cfg, "Transaction will be rollback.");
            }
        }

        return true;
    }
}
