package com.tonyzampogna.factory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy;
import com.tonyzampogna.config.ListsDatabaseProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * This factory will return an instance of the Cassandra Session object.
 * There is one instance per keyspace per application. So, we keep a map
 * of the instances by keyspace name.
 */
@Component
public class ListsDatabaseSessionFactory {
	private static final Logger log = LoggerFactory.getLogger(ListsDatabaseSessionFactory.class);


	private static Session SESSION = null;

	@Autowired
	private ListsDatabaseProperties listsDatabaseProperties;


	/**
	 * Get the Cassandra session for the keyspace name.
	 */
	public Session getSession() {

		if (ListsDatabaseSessionFactory.SESSION == null) {
			synchronized(Session.class) {
				try {
					String cassandraNodes = listsDatabaseProperties.getCassandraNodes();
					String keyspace = listsDatabaseProperties.getKeyspaceName();
					String username = listsDatabaseProperties.getUsername();
					String password = listsDatabaseProperties.getPassword();
					int connectionsPerHost = listsDatabaseProperties.getConnectionsPerHost();
					int coreConnectionsPerHost = listsDatabaseProperties.getCoreConnectionsPerHost();
					int maxConnectionsPerHost = listsDatabaseProperties.getMaxConnectionsPerHost();
					int maxRequestsPerConnection = listsDatabaseProperties.getMaxRequestsPerConnection();

					Cluster.Builder builder = Cluster.builder()
						.addContactPoints(cassandraNodes.split(","))
						.withReconnectionPolicy(new ExponentialReconnectionPolicy(1000, 30000));

					PoolingOptions poolingOptions = new PoolingOptions();
					poolingOptions.setConnectionsPerHost(HostDistance.LOCAL, connectionsPerHost, maxConnectionsPerHost);
					poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, coreConnectionsPerHost);
					poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, maxRequestsPerConnection);
					builder.withPoolingOptions(poolingOptions);

					if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
						builder.withCredentials(username, password);
					}

					// Get the Session
					Cluster cluster = builder.build();

					// Connect to the DB, and set the static variable.
					ListsDatabaseSessionFactory.SESSION = cluster.connect(keyspace);
				}
				catch (Exception e) {
					log.error("Exception occurred while getting Cassandra session for the Lists database.", e);
				}
			}
		}

		return ListsDatabaseSessionFactory.SESSION;
	}

}
