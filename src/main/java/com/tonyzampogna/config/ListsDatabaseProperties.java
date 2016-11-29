package com.tonyzampogna.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties file for Cassandra
 */
@ConfigurationProperties("cassandra.properties")
@Component
public class ListsDatabaseProperties {

	private String cassandraNodes = null;
	private String keyspaceName = null;
	private String username = null;
	private String password = null;

	private Integer connectionsPerHost = null;
	private Integer coreConnectionsPerHost = null;
	private Integer maxConnectionsPerHost = null;
	private Integer maxRequestsPerConnection = null;


	public String getCassandraNodes() {
		return cassandraNodes;
	}

	public void setCassandraNodes(String cassandraNodes) {
		this.cassandraNodes = cassandraNodes;
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
