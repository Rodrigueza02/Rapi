
package aggregator.ports;

import aggregator.model.OrderView;

public interface DashboardBroadcastPort {

    void broadcast(OrderView orderView);
}
