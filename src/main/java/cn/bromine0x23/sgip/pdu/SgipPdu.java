package cn.bromine0x23.sgip.pdu;

import cn.bromine0x23.sgip.SgipConstants;
import cn.bromine0x23.sgip.exception.RecoverablePduException;
import cn.bromine0x23.sgip.exception.UnrecoverablePduException;
import cn.bromine0x23.sgip.util.HexUtil;
import io.netty.buffer.ByteBuf;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PDU基类
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@EqualsAndHashCode
public abstract class SgipPdu {

	private static AtomicInteger sequenceNumberCounter = new AtomicInteger();

	private final boolean request;

	/**
	 * 消息名，不参与编码
	 */
	@Getter
	private String name;

	/**
	 * 命令消息的总长度（4 字节）
	 */
	@Setter
	private Integer commandLength;

	/**
	 * 命令ID（4 字节）
	 */
	@Getter
	private final int commandId;

	/**
	 * 命令源节点编号（4 字节）
	 */
	@Setter
	private Integer sourceNodeId;

	/**
	 * 命令产生的日期和时间（4 字节）
	 */
	@Setter
	private Integer timestamp;

	/**
	 * 流水号（4 字节）
	 */
	@Setter
	private Integer sequenceNumber;

	public SgipPdu(int commandId, String name, boolean request) {
		this.name = name;
		this.commandId = commandId;
		this.request = request;
	}

	public boolean isRequest() {
		return request;
	}

	public boolean isResponse() {
		return !request;
	}

	public int getCommandLength() {
		if (commandLength == null) {
			return 0;
		} else {
			return commandLength;
		}
	}

	public int getSourceNodeId() {
		if (sourceNodeId == null) {
			return 0;
		} else {
			return sourceNodeId;
		}
	}

	public int getTimestamp() {
		if (timestamp == null) {
			this.timestamp = calculateTimestamp();
		}
		return timestamp;
	}

	public int getSequenceNumber() {
		if (sequenceNumber == null) {
			this.sequenceNumber = sequenceNumberCounter.incrementAndGet();
		}
		return sequenceNumber;
	}

	public boolean hasCommandLengthCalculated() {
		return commandLength != null;
	}

	public boolean hasSourceNodeIdAssigned() {
		return sourceNodeId != null;
	}

	public boolean hasTimestampAssigned() {
		return timestamp != null;
	}

	public boolean hasSequenceNumberAssigned() {
		return sequenceNumber != null;
	}

	public void calculateAndSetCommandLength() {
		int length = SgipConstants.PDU_HEADER_LENGTH + this.calculateByteSizeOfBody();
		this.setCommandLength(length);
	}

	public void writeHeader(ByteBuf buffer) {
		buffer.writeInt(getCommandLength());
		buffer.writeInt(getCommandId());
		buffer.writeInt(getSourceNodeId());
		buffer.writeInt(getTimestamp());
		buffer.writeInt(getSequenceNumber());
	}

	protected abstract int calculateByteSizeOfBody();

	public abstract void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException;

	public abstract void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException;

	protected abstract void appendBodyToString(StringBuilder builder);

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append("(");
		builder.append("CommandLength=0x");
		HexUtil.appendHexString(builder, getCommandLength());
		builder.append(", CommandId=0x");
		HexUtil.appendHexString(builder, getCommandId());
		builder.append(", SourceNodeId=0x");
		HexUtil.appendHexString(builder, getSourceNodeId());
		builder.append(", Timestamp=0x");
		HexUtil.appendHexString(builder, getTimestamp());
		builder.append(", SequenceNumber=0x");
		HexUtil.appendHexString(builder, getSequenceNumber());
		builder.append(", Body=");
		appendBodyToString(builder);
		builder.append(")");
		return builder.toString();
	}

	public static Integer calculateTimestamp() {
		Calendar calendar = Calendar.getInstance();
		int      month    = calendar.get(Calendar.MONTH) + 1;
		int      day      = calendar.get(Calendar.DAY_OF_MONTH);
		int      hour     = calendar.get(Calendar.HOUR_OF_DAY);
		int      minute   = calendar.get(Calendar.MINUTE);
		int      second   = calendar.get(Calendar.SECOND);
		return (((month * 100 + day) * 100 + hour) * 100 + minute) * 100 + second;
	}
}
