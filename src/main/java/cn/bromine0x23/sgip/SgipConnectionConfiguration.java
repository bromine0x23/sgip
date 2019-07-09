/*
 * Copyright © 2017-2019 Bromine0x23 <bromine0x23@163.com>
 * This work is free. You can redistribute it and/or modify it under the
 * terms of the Do What The Fuck You Want To Public License, Version 2,
 * as published by Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */
package cn.bromine0x23.sgip;

import lombok.Data;

/**
 * SGIP连接配置
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@Data
public class SgipConnectionConfiguration {

	/**
	 * 服务器地址
	 */
	private String host;

	/**
	 * 服务器端口
	 */
	private int port;

	/**
	 * 连接超时时间
	 */
	private int connectTimeout = SgipConstants.DEFAULT_CONNECT_TIMEOUT;
}
