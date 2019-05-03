package cn.bromine0x23.sgip.pdu;

import cn.bromine0x23.sgip.SgipConstants;
import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import cn.bromine0x23.sgip.util.ByteBufUtil;
import cn.bromine0x23.sgip.util.HexUtil;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Bind 命令
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
public class SgipBind extends SgipPduRequest<SgipBindResp> {

	/**
	 * 登录类型（1 Byte）
	 * <p>
	 * 1：SP向SMG建立的连接，用于发送命令<br/>
	 * 2：SMG向SP建立的连接，用于发送命令<br/>
	 * 3：SMG之间建立的连接，用于转发命令<br/>
	 * 4：SMG向GNS建立的连接，用于路由表的检索和维护<br/>
	 * 5：GNS向SMG建立的连接，用于路由表的更新<br/>
	 * 6：主备GNS之间建立的连接，用于主备路由表的一致性<br/>
	 * 11：SP与SMG以及SMG之间建立的测试连接，用于跟踪测试<br/>
	 * 其它：保留
	 */
	@Getter
	@Setter
	private byte loginType;

	/**
	 * 服务器端给客户端分配的登录名（16 Byte）
	 */
	@Getter
	private String loginName;

	/**
	 * 服务器端和Login Name对应的密码（16 Byte）
	 */
	@Getter
	private String loginPassword;

	/**
	 * 保留，扩展用（8 Byte）
	 */
	@Getter
	@Setter
	private String reserve;

	public SgipBind() {
		super(SgipConstants.COMMAND_ID_BIND, "Bind");
	}

	public void setLoginName(String loginName) {
		if (loginName.length() > 16) {
			throw new IllegalArgumentException();
		}
		this.loginName = loginName;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	@Override
	public SgipBindResp createResponse() {
		return new SgipBindResp();
	}

	@Override
	public Class<SgipBindResp> getResponseClass() {
		return SgipBindResp.class;
	}

	@Override
	protected int calculateByteSizeOfBody() {
		return 1 + 16 + 16 + 8;
	}

	@Override
	public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		this.loginType = buffer.readByte();
		this.loginName = ByteBufUtil.readFixedString(buffer, 16);
		this.loginPassword = ByteBufUtil.readFixedString(buffer, 16);
		this.reserve = ByteBufUtil.readFixedString(buffer, 8);
	}

	@Override
	public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		buffer.writeByte(loginType);
		ByteBufUtil.writeFixedString(buffer, loginName, 16);
		ByteBufUtil.writeFixedString(buffer, loginPassword, 16);
		ByteBufUtil.writeFixedString(buffer, reserve, 8);
	}

	@Override
	protected void appendBodyToString(StringBuilder builder) {
		builder.append("(LoginType=0x");
		HexUtil.appendHexString(builder, getLoginType());
		builder.append(", LoginName=");
		builder.append(getLoginName());
		builder.append(", LoginPassword=");
		builder.append(getLoginPassword());
		builder.append(", Reserve=");
		builder.append(getReserve());
		builder.append(")");
	}
}
