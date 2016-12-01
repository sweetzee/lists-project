package com.tonyzampogna.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Properties file for Cassandra
 */
@Component
public class ListsDatabaseProperties {

	@Value("${lists.database.nodes}")
	private String nodes = null;

	@Value("${lists.database.keyspace}")
	private String keyspaceName = null;

	@Value("${lists.database.username}")
	private String username = null;

	@Value("${lists.database.password}")
	private String password = null;

	@Value("${lists.database.connections-per-host}")
	private Integer connectionsPerHost = null;

	@Value("${lists.database.core-connections-per-host}")
	private Integer coreConnectionsPerHost = null;

	@Value("${lists.database.max-connections-per-host}")
	private Integer maxConnectionsPerHost = null;

	@Value("${lists.database.max-requests-per-connection}")
	private Integer maxRequestsPerConnection = null;


	public String getNodes() {
		return nodes;
	}

	public void setCassandraNodes(String cassandraNodes) {
		this.nodes = nodes;
	}

	public String getKeyspaceName() {
		return keyspaceName;
	}

	public void setKeyspaceName(String keyspaceName) {
		this.keyspaceName = keyspaceName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getConnectionsPerHost() {
		return connectionsPerHost;
	}

	public void setConnectionsPerHost(Integer connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public Integer getCoreConnectionsPerHost() {
		return coreConnectionsPerHost;
	}

	public void setCoreConnectionsPerHost(Integer coreConnectionsPerHost) {
		this.coreConnectionsPerHost = coreConnectionsPerHost;
	}

	public Integer getMaxConnectionsPerHost() {
		return maxConnectionsPerHost;
	}

	public void setMaxConnectionsPerHost(Integer maxConnectionsPerHost) {
		this.maxConnectionsPerHost = maxConnectionsPerHost;
	}

	public Integer getMaxRequestsPerConnection() {
		return maxRequestsPerConnection;
	}

	public void setMaxRequestsPerConnection(Integer maxRequestsPerConnection) {
		this.maxRequestsPerConnection = maxRequestsPerConnection;
	}
}
