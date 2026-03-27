package com.xinyi.device.utils;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.xinyi.device.DeviceContext;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Shell工具类
 *
 * <p>
 *   提供了执行 shell 命令的功能，包括判断 ADB 是否可用、检查是否拥有 root 权限、执行单条或多条命令等。
 * </p>
 *
 * @author 新一
 * @date 2022/5/12 10:34
 */
public final class ShellUtil {

    private final static String TAG = ShellUtil.class.getSimpleName();

    /**
     * 是否使用 root 权限
     */
    private static Boolean isRoot = null;

    private ShellUtil() { }

    /**
     * 判断设备 ADB 是否可用
     *
     * @return {@code true}：ADB 可用，{@code false}：ADB 不可用
     */
    public static boolean isAdbEnabled() {
        // 获取 ADB 设置值，如果值大于 0，则说明 ADB 已启用
        Context context = DeviceContext.getApplication();
        return Settings.Secure.getInt(context.getContentResolver(),
                Settings.Global.ADB_ENABLED, 0
        ) > 0;
    }

    /**
     * 检查设备是否拥有 root 权限，该方法通过多种方式检测 root 权限。
     *
     *  <li>{@link #checkSuFile()} 快速检测 su 是否存在（最简单）</li>
     *  <li>{@link #isRootByExec()} 尝试执行 id（最可靠）</li>
     *  <li>{@link #isRootByProperty()} 检查 ro.secure 和 ro.debuggable（适用于某些 ROM）</li>
     * @return {@code true} 设备已 root，{@code false} 设备未 root
     */
    public static boolean isCheckRoot() {
        // 如果已经检测过 root，则直接返回缓存结果，避免重复执行
        if (isRoot != null) {
            return isRoot;
        }

        // 依次执行三种 root 检测方式
        isRoot =  checkSuFile() || isRootByExec() || isRootByProperty();
        return isRoot;
    }

    /**
     * 方式 1：检查 `su` 文件是否存在
     * 在常见的系统目录下查找 `su` 文件，判断设备是否 root
     *
     * @return {@code true} 发现 `su` 文件，{@code false} 未发现 `su` 文件
     */
    public static boolean checkSuFile() {
        // 常见的 root 目录，通常 root 权限的 su 文件会存放在这些目录中
        String[] paths = {
                "/system/bin/", "/system/xbin/", "/sbin/",
                "/system/sd/xbin/", "/system/bin/failsafe/",
                "/data/local/xbin/", "/data/local/bin/",
                "/system/sbin/", "/usr/bin/", "/vendor/bin/"
        };

        for (String path : paths) {
            File suFile = new File(path + "su");
            if (suFile.exists() && suFile.canExecute()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 方式 2：尝试执行 `su` 命令
     * 该方法尝试执行 `su` 命令，并检查是否可以正常执行，以判断设备是否 root
     *
     * @return {@code true} 可以执行 `su` 命令，{@code false} 无法执行 `su` 命令
     */
    public static boolean isRootByExec() {
        CommandResult result = execCommand("su -c id", true);
        // 0 代表成功，说明设备已 root
        return result.result == 0;
    }

    /**
     * 方式 3：读取系统属性，判断设备是否可能已 root
     * 通过 `getprop` 读取 `ro.secure` 和 `ro.debuggable` 系统属性：
     * - `ro.secure=0` 可能表示设备已 root
     * - `ro.debuggable=1` 可能表示设备处于开发者模式，有更高的权限
     *
     * @return {@code true} 可能已 root，{@code false} 可能未 root
     */
    public static boolean isRootByProperty() {
        try {
            String secureProp = getSystemProperty("ro.secure");
            String debuggableProp = getSystemProperty("ro.debuggable");

            return "0".equals(secureProp) || "1".equals(debuggableProp);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 获取系统属性值
     * 通过 `getprop` 命令获取系统属性，用于判断设备是否可能已 root
     *
     * @param propName 属性名称
     * @return 属性值，如果获取失败则返回空字符串
     * @throws IOException 读取异常
     */
    private static String getSystemProperty(String propName) throws IOException {
        Process process = Runtime.getRuntime().exec(new String[]{"getprop", propName});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String propValue = reader.readLine(); // 读取属性值
        reader.close();
        return propValue != null ? propValue : "";
    }

    /**
     * 创建 Shell 进程
     *
     * @param isRoot 是否使用 root 权限
     * @return 进程对象
     */
    public static Process createShellProcess(boolean isRoot) {
        try {
            return Runtime.getRuntime().exec(isRoot ? "su" : "sh");
        } catch (IOException e) {
            Log.e(TAG, "创建 Shell 进程失败", e);
            return null;
        }
    }

    /**
     * 执行单条命令
     *
     * @param command 命令字符串
     * @param isRoot  是否需要 root 权限
     * @return 命令执行结果 {@link CommandResult}
     */
    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot, true);
    }

    /**
     * 执行多条命令
     *
     * @param commands 多条命令
     * @param isRoot   是否需要 root 权限
     * @return 命令执行结果 {@link CommandResult}
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot, true);
    }

    /**
     * 执行多条命令
     *
     * @param commands 多条命令
     * @param isRoot   是否需要 root 权限
     * @return 命令执行结果 {@link CommandResult}
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }

    /**
     * 执行命令
     *
     * @param command         命令
     * @param isRoot          是否需要 root 权限
     * @param isNeedResultMsg 是否需要结果消息
     * @return 命令执行结果 {@link CommandResult}
     */
    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }

    /**
     * 执行多条命令
     *
     * @param commands        多条命令
     * @param isRoot          是否需要 root 权限
     * @param isNeedResultMsg 是否需要结果消息
     * @return 命令执行结果 {@link CommandResult}
     */
    public static CommandResult execCommand(List<String> commands, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : commands.toArray(new String[]{}), isRoot, isNeedResultMsg);
    }

    /**
     * 执行多条命令
     * 该方法会根据是否需要 root 权限来执行命令，并且可以指定是否需要返回结果
     *
     * @param commands        多条命令
     * @param isRoot          是否需要 root 权限
     * @param isNeedResultMsg 是否需要返回执行结果
     * @return 命令执行结果 {@link CommandResult}
     */
    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        if (commands == null || commands.length == 0) {
            return new CommandResult(-1, null, null);
        }

        // 进程对象
        Process process = null;
        // 结果状态码
        int result = -1;
        // 成功的信息
        StringBuilder successBuilder = null;
        // 错误的信息
        StringBuilder errorBuilder = null;

        try {
            // 创建进程
            process = createShellProcess(isRoot);
            if (process == null) {
                return new CommandResult(-1, null, "无法创建 Shell 进程");
            }
            try (DataOutputStream os = new DataOutputStream(process.getOutputStream())) {
                // 写入命令
                writeCommandsToShell(os, commands);
                // 等待执行完成
                result = process.waitFor();

                // 读取命令输出
                if (isNeedResultMsg) {
                    // 读取命令执行的成功消息
                    successBuilder = readStream(process.getInputStream());
                    // 读取命令执行的错误消息
                    errorBuilder = readStream(process.getErrorStream());
                }
            }
        } catch (Exception exception) {
            Log.e(TAG, "执行 Shell 命令时发生异常", exception);
        } finally {
            // 销毁进程
            if (process != null) {
                process.destroy();
            }
        }
        String successMsg = successBuilder == null ? null : successBuilder.toString();
        String errorMsg = errorBuilder == null ? null : errorBuilder.toString();
        return new CommandResult(result, successMsg, errorMsg);
    }

    /**
     * 读取输入流
     *
     * @param inputStream 输入流
     * @return 读取的字符串内容
     * @throws IOException 读取异常
     */
    private static StringBuilder readStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output;
    }

    /**
     * 向 Shell 进程中写入命令
     *
     * @param os       数据流
     * @param commands 需要执行的命令
     * @throws IOException 可能抛出的异常
     */
    private static void writeCommandsToShell(DataOutputStream os, String[] commands) throws IOException {
        for (String command : commands) {
            if (command == null) {
                // 忽略空命令
                continue;
            }
            // 写入命令
            os.write(command.getBytes());
            // 每条命令后加换行符
            os.writeBytes("\n");
            // 强制刷新
            os.flush();
        }
        // 写入退出命令并等待进程完成
        os.writeBytes("exit\n");
        os.flush();
    }

    /**
     * 返回的命令结果
     */
    public static class CommandResult {

        /**
         * 命令执行结果码，0 表示成功
         */
        public int result;

        /**
         * 成功信息
         */
        public String successMsg;

        /**
         * 错误信息
         */
        public String errorMsg;

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }
}