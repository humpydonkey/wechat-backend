package data.db;

/**
 * MongoConn Config
 *
 * Created by yazhoucao on 8/7/16.
 */

public interface DBConfig {
  String getDBName();
  String getIP();
  int getPort();

  /**
   * The connection timeout in milliseconds. A value of 0 means no timeout.
   * It is used solely when establishing a new connection Socket.connect(java.net.SocketAddress, int)
   */
  default int getConnectTime() {
    return 1000_000; // ms
  }

  /**
   * The maximum wait time in milliseconds that a thread may wait for a connection to become available.
   */
  default int getMaxWaitTime() {
    return 60_000; // ms
  }
}
