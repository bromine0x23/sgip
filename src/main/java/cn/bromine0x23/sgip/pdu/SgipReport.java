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
 * Report 命令
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
public class SgipReport extends SgipPduRequest<SgipReportResp> {

	/**
	 * 该命令所涉及的Submit或Deliver命令的源节点编号（4 字节）
	 */
	@Getter
	@Setter
	private int submitSourceNodeId;

	/**
	 * 该命令所涉及的Submit或Deliver命令的产生时间戳（4 字节）
	 */
	@Getter
	@Setter
	private int submitTimestamp;

	/**
	 * 该命令所涉及的Submit或Deliver命令的流水号（4 字节）
	 */
	@Getter
	@Setter
	private int submitSequenceNumber;

	/**
	 * Report命令类型（1 字节）
	 * <p>
	 * 0：对先前一条Submit命令的状态报告
	 * 1：对先前一条前转Deliver命令的状态报告
	 */
	@Getter
	@Setter
	private int reportType;

	/**
	 * 接收短消息的手机号（21 字节）
	 * <p>
	 * 手机号码前加“86”国别标志
	 */
	@Getter
	@Setter
	private String userNumber;

	/**
	 * 该命令所涉及的短消息的当前执行状态（1 字节）
	 * <p>
	 * 0：发送成功
	 * 1：等待发送
	 * 2：发送失败
	 */
	@Getter
	@Setter
	private int state;

	/**
	 * 当State=2时为错误码值，否则为0（1 字节）
	 */
	@Getter
	@Setter
	private int errorCode;

	/**
	 * 保留，扩展用（8 Byte）
	 */
	@Getter
	@Setter
	private String reserve;

	public SgipReport() {
		super(SgipConstants.COMMAND_ID_REPORT, "Report");
	}

	@Override
	public SgipReportResp createResponse() {
		return new SgipReportResp();
	}

	@Override
	public Class<SgipReportResp> getResponseClass() {
		return SgipReportResp.class;
	}

	@Override
	protected int calculateByteSizeOfBody() {
		return 4 + 4 + 4 + 1 + 21 + 1 + 1 + 8;
	}

	@Override
	public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		this.submitSourceNodeId = buffer.readInt();
		this.submitTimestamp = buffer.readInt();
		this.submitSequenceNumber = buffer.readInt();
		this.reportType = buffer.readByte();
		this.userNumber = ByteBufUtil.readFixedString(buffer, 21);
		this.state = buffer.readByte();
		this.errorCode = buffer.readByte();
		this.reserve = ByteBufUtil.readFixedString(buffer, 21);
	}

	@Override
	public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
		buffer.writeInt(getSubmitSequenceNumber());
		buffer.writeInt(getSubmitTimestamp());
		buffer.writeInt(getSubmitSequenceNumber());
		buffer.writeByte(getReportType());
		ByteBufUtil.writeFixedString(buffer, getUserNumber(), 21);
		buffer.writeByte(getState());
		buffer.writeByte(getErrorCode());
		ByteBufUtil.writeFixedString(buffer, getReserve(), 8);
	}

	@Override
	protected void appendBodyToString(StringBuilder builder) {
		builder.append("(SubmitSourceNodeId=0x");
		HexUtil.appendHexString(builder, getSubmitSequenceNumber());
		builder.append(", SubmitTimestamp=0x");
		HexUtil.appendHexString(builder, getSubmitTimestamp());
		builder.append(", SubmitSequenceNumber=0x");
		HexUtil.appendHexString(builder, getSubmitSequenceNumber());
		builder.append(", ReportType=0x");
		HexUtil.appendHexString(builder, getReportType());
		builder.append(", UserNumber=");
		builder.append(getUserNumber());
		builder.append(", State=0x");
		HexUtil.appendHexString(builder, getState());
		builder.append(", ErrorCode=0x");
		HexUtil.appendHexString(builder, getErrorCode());
		builder.append(", Reserve=");
		builder.append(getReserve());
		builder.append(")");
	}
}
