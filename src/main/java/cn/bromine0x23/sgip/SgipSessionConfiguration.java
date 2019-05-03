package cn.bromine0x23.sgip;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * SGIP会话配置
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SgipSessionConfiguration extends SgipConnectionConfiguration {

	/**
	 * 源节点编号
	 */
	private String name;

	/**
	 * 源节点编号
	 */
	private int sourceNodeId;

	/**
	 * 登录类型
	 *
	 * @see SgipLoginType
	 */
	private SgipLoginType loginType;

	/**
	 * 登录名
	 */
	private String loginName;

	/**
	 * 登录密码
	 */
	private String loginPassword;

	/**
	 * 是否记录PDU日志
	 */
	private boolean logPduEnabled = false;

	private boolean logBytesEnabled = true;

	/**
	 * Bind超时时间
	 */
	private long bindTimeout = SgipConstants.DEFAULT_BIND_TIMEOUT;

	/**
	 * 写超时时间
	 */
	private long writeTimeout = SgipConstants.DEFAULT_WRITE_TIMEOUT;

	/**
	 * 请求过期时间
	 */
	private long requestExpiryTimeout = SgipConstants.DEFAULT_REQUEST_EXPIRY_TIMEOUT;

	/**
	 * 发送窗口过期时间
	 */
	private long windowWaitTimeout = SgipConstants.DEFAULT_WINDOW_WAIT_TIMEOUT;

	/**
	 * 发送窗口大小
	 */
	private int windowSize = SgipConstants.DEFAULT_WINDOW_SIZE;

	private long windowMonitorInterval = SgipConstants.DEFAULT_WINDOW_MONITOR_INTERVAL;
}
