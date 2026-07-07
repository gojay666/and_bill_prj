package com.bill_prj.utils;

import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 数据备份与恢复工具类
 * 提供数据备份、恢复、CSV导出等功能
 */
public class DataBackupUtils {

    private static final String BACKUP_DIR = "backups";
    private static final String EXPORT_DIR = "exports";
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setPrettyPrinting()
            .create();

    /**
     * 备份数据到 JSON 文件
     *
     * @param context  上下文
     * @param jsonData 需要备份的 JSON 格式数据字符串
     * @return 备份文件，失败返回 null
     */
    public static File backupData(Context context, String jsonData) {
        try {
            File backupDir = getBackupDir(context);
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                return null;
            }

            String fileName = Constants.BACKUP_FILE_PREFIX
                    + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date())
                    + Constants.BACKUP_FILE_EXTENSION;

            File backupFile = new File(backupDir, fileName);

            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(backupFile), StandardCharsets.UTF_8)) {
                writer.write(jsonData);
                writer.flush();
            }

            return backupFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 备份数据（传入泛型对象，自动转换为 JSON）
     *
     * @param context 上下文
     * @param data    需要备份的数据对象
     * @param <T>     数据类型
     * @return 备份文件，失败返回 null
     */
    public static <T> File backupData(Context context, T data) {
        String json = gson.toJson(data);
        return backupData(context, json);
    }

    /**
     * 从 JSON 备份文件中恢复数据
     *
     * @param context    上下文
     * @param backupFile 备份文件
     * @return JSON 字符串格式的数据，失败返回 null
     */
    public static String restoreData(Context context, File backupFile) {
        if (backupFile == null || !backupFile.exists() || !backupFile.isFile()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(backupFile), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 JSON 备份文件恢复数据为指定类型对象
     *
     * @param context    上下文
     * @param backupFile 备份文件
     * @param clazz      目标类型
     * @param <T>        目标类型
     * @return 反序列化后的对象，失败返回 null
     */
    public static <T> T restoreData(Context context, File backupFile, Class<T> clazz) {
        String json = restoreData(context, backupFile);
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 JSON 备份文件恢复数据为指定类型列表
     *
     * @param context    上下文
     * @param backupFile 备份文件
     * @param clazz      列表元素类型
     * @param <T>        列表元素类型
     * @return 反序列化后的对象列表，失败返回 null
     */
    public static <T> List<T> restoreDataList(Context context, File backupFile, Class<T> clazz) {
        String json = restoreData(context, backupFile);
        if (json == null) {
            return null;
        }
        try {
            Type type = TypeToken.getParameterized(List.class, clazz).getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将数据导出为 CSV 文件
     *
     * @param headers    CSV 列头
     * @param dataRows   数据行（每行为一个字符串数组）
     * @param context    上下文
     * @param fileName   文件名（不含路径）
     * @return 生成的 CSV 文件，失败返回 null
     */
    public static File exportToCsv(String[] headers, List<String[]> dataRows,
                                   Context context, String fileName) {
        FileWriter writer = null;
        try {
            File exportDir = getExportDir(context);
            if (!exportDir.exists() && !exportDir.mkdirs()) {
                return null;
            }

            File csvFile = new File(exportDir, fileName);

            writer = new FileWriter(csvFile, StandardCharsets.UTF_8);

            // 写入 BOM 头，防止 Excel 打开乱码
            writer.write('\ufeff');

            // 写入列头
            if (headers != null && headers.length > 0) {
                writer.write(joinCsvRow(headers));
                writer.write("\r\n");
            }

            // 写入数据行
            if (dataRows != null) {
                for (String[] row : dataRows) {
                    if (row != null && row.length > 0) {
                        writer.write(joinCsvRow(row));
                        writer.write("\r\n");
                    }
                }
            }

            writer.flush();
            return csvFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将数据列表导出为 CSV 文件
     *
     * @param headers    CSV 列头
     * @param dataRows   数据行（每行为一个字符串数组）
     * @param context    上下文
     * @return 生成的 CSV 文件，失败返回 null
     */
    public static File exportToCsv(String[] headers, List<String[]> dataRows, Context context) {
        String fileName = Constants.CSV_FILE_PREFIX
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date())
                + Constants.CSV_FILE_EXTENSION;
        return exportToCsv(headers, dataRows, context, fileName);
    }

    /**
     * 将 CSV 文件的一行转义并拼接
     */
    private static String joinCsvRow(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(Constants.CSV_DELIMITER);
            }
            String field = fields[i] != null ? fields[i] : "";
            // 如果包含逗号、引号或换行符，需要包裹在引号中
            if (field.contains(Constants.CSV_DELIMITER)
                    || field.contains("\"")
                    || field.contains("\n")
                    || field.contains("\r")) {
                field = field.replace("\"", "\"\"");
                sb.append("\"").append(field).append("\"");
            } else {
                sb.append(field);
            }
        }
        return sb.toString();
    }

    /**
     * 读取 CSV 文件内容
     *
     * @param csvFile CSV 文件
     * @return 解析后的数据行列表（第一行为列头），失败返回 null
     */
    public static List<String[]> readCsvFile(File csvFile) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                // 跳过 BOM 头
                if (line.startsWith("\ufeff")) {
                    line = line.substring(1);
                }
                rows.add(parseCsvLine(line));
            }
            return rows;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析 CSV 一行数据
     */
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    // 检查是否为转义引号
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        currentField.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    currentField.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == Constants.CSV_DELIMITER.charAt(0)) {
                    fields.add(currentField.toString().trim());
                    currentField = new StringBuilder();
                } else {
                    currentField.append(c);
                }
            }
        }
        fields.add(currentField.toString().trim());
        return fields.toArray(new String[0]);
    }

    /**
     * 获取所有备份文件列表（按最后修改时间降序排列）
     *
     * @param context 上下文
     * @return 备份文件列表
     */
    public static List<File> getBackupFiles(Context context) {
        List<File> backupFiles = new ArrayList<>();
        File backupDir = getBackupDir(context);
        if (backupDir.exists() && backupDir.isDirectory()) {
            File[] files = backupDir.listFiles((dir, name) ->
                    name.startsWith(Constants.BACKUP_FILE_PREFIX)
                            && name.endsWith(Constants.BACKUP_FILE_EXTENSION));
            if (files != null) {
                for (File file : files) {
                    backupFiles.add(file);
                }
                // 按最后修改时间降序排列
                backupFiles.sort((f1, f2) ->
                        Long.compare(f2.lastModified(), f1.lastModified()));
            }
        }
        return backupFiles;
    }

    /**
     * 获取备份目录
     */
    public static File getBackupDir(Context context) {
        File externalDir = context.getExternalFilesDir(null);
        if (externalDir != null) {
            return new File(externalDir, BACKUP_DIR);
        }
        return new File(context.getFilesDir(), BACKUP_DIR);
    }

    /**
     * 获取导出目录
     */
    public static File getExportDir(Context context) {
        File externalDir = context.getExternalFilesDir(null);
        if (externalDir != null) {
            return new File(externalDir, EXPORT_DIR);
        }
        return new File(context.getFilesDir(), EXPORT_DIR);
    }

    /**
     * 获取公共下载目录中的备份文件（用于用户通过文件管理器访问）
     */
    public static File getPublicBackupDir() {
        File publicDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        File backupDir = new File(publicDir, BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        return backupDir;
    }

    /**
     * 将数据导出到公共下载目录
     *
     * @param context  上下文
     * @param jsonData JSON 格式数据
     * @return 备份文件，失败返回 null
     */
    public static File backupDataToPublic(Context context, String jsonData) {
        try {
            File backupDir = getPublicBackupDir();
            if (!backupDir.exists() && !backupDir.mkdirs()) {
                return backupData(context, jsonData);
            }

            String fileName = Constants.BACKUP_FILE_PREFIX
                    + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date())
                    + Constants.BACKUP_FILE_EXTENSION;

            File backupFile = new File(backupDir, fileName);

            try (Writer writer = new OutputStreamWriter(
                    new FileOutputStream(backupFile), StandardCharsets.UTF_8)) {
                writer.write(jsonData);
                writer.flush();
            }

            return backupFile;
        } catch (IOException e) {
            e.printStackTrace();
            return backupData(context, jsonData);
        }
    }

    /**
     * 删除备份文件
     *
     * @param backupFile 备份文件
     * @return 是否删除成功
     */
    public static boolean deleteBackupFile(File backupFile) {
        return backupFile != null && backupFile.exists() && backupFile.delete();
    }

    /**
     * 获取备份文件大小（格式化字符串）
     *
     * @param backupFile 备份文件
     * @return 文件大小字符串，如 "1.23 MB"
     */
    public static String getBackupFileSize(File backupFile) {
        if (backupFile == null || !backupFile.exists()) {
            return "0 B";
        }
        long size = backupFile.length();
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format(Locale.getDefault(), "%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 任意对象
     * @return JSON 字符串
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    /**
     * 从 JSON 字符串解析为指定类型的对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 解析后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
