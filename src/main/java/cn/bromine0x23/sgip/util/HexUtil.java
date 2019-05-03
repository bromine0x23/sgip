package cn.bromine0x23.sgip.util;

/**
 * 16进制格式数字工具
 *
 * @author <a href="mailto:bromine0x23@163.com">Bromine0x23</a>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class HexUtil {

	private static final char[] HEX_TABLE = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	private HexUtil() {
	}

	public static String toHexString(byte value) {
		StringBuilder builder = new StringBuilder(2);
		appendHexString(builder, value);
		return builder.toString();
	}

	public static void appendHexString(StringBuilder builder, byte value) {
		builder.append(HEX_TABLE[(value & 0xF0) >>> 4]);
		builder.append(HEX_TABLE[(value & 0x0F)]);
	}

	public static String toHexString(short value) {
		StringBuilder builder = new StringBuilder(4);
		appendHexString(builder, value);
		return builder.toString();
	}

	public static void appendHexString(StringBuilder builder, short value) {
		builder.append(HEX_TABLE[(value & 0xF000) >>> 12]);
		builder.append(HEX_TABLE[(value & 0x0F00) >>> 8]);
		builder.append(HEX_TABLE[(value & 0x00F0) >>> 4]);
		builder.append(HEX_TABLE[(value & 0x000F)]);
	}

	public static String toHexString(int value) {
		StringBuilder builder = new StringBuilder(8);
		appendHexString(builder, value);
		return builder.toString();
	}

	public static void appendHexString(StringBuilder builder, int value) {
		builder.append(HEX_TABLE[(value & 0xF0000000) >>> 28]);
		builder.append(HEX_TABLE[(value & 0x0F000000) >>> 24]);
		builder.append(HEX_TABLE[(value & 0x00F00000) >>> 20]);
		builder.append(HEX_TABLE[(value & 0x000F0000) >>> 16]);
		builder.append(HEX_TABLE[(value & 0x0000F000) >>> 12]);
		builder.append(HEX_TABLE[(value & 0x00000F00) >>> 8]);
		builder.append(HEX_TABLE[(value & 0x000000F0) >>> 4]);
		builder.append(HEX_TABLE[(value & 0x0000000F)]);
	}

	public static String toHexString(long value) {
		StringBuilder buffer = new StringBuilder(16);
		appendHexString(buffer, value);
		return buffer.toString();
	}

	public static void appendHexString(StringBuilder buffer, long value) {
		appendHexString(buffer, (int)((value & 0xFFFFFFFF00000000L) >>> 32));
		appendHexString(buffer, (int)(value & 0x00000000FFFFFFFFL));
	}

}
