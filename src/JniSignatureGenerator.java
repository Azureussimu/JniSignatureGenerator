import java.util.HashMap;
import java.util.Map;

public class JniSignatureGenerator {

    // 使用不可变Map提高性能
    private static final Map<Class<?>, String> PRIMITIVE_SIGNATURES = Map.of(
            void.class, "V",
            boolean.class, "Z",
            byte.class, "B",
            char.class, "C",
            short.class, "S",
            int.class, "I",
            long.class, "J",
            float.class, "F",
            double.class, "D"
    );

    // 缓存非数组类型的签名
    private static final Map<String, String> CLASS_SIGNATURE_CACHE = new HashMap<>();

    /**
     * 生成带方法名的完整JNI签名
     * @param methodName 方法名
     * @param returnType 返回类型（基本类型或类类型）
     * @param paramTypes 参数类型数组
     * @return JNI方法签名，如：methodName(Ljava/lang/String;I)Ljava/lang/String;
     */
    public static String generateSignatureWithMethodName(String methodName, Class<?> returnType, Class<?>... paramTypes) {
        if (methodName == null || methodName.isEmpty()) {
            throw new IllegalArgumentException("Method name cannot be null or empty");
        }
        return methodName + generateSignature(returnType, paramTypes);
    }

    /**
     * 生成JNI方法签名（不带方法名）
     * @param returnType 返回类型
     * @param paramTypes 参数类型数组
     * @return JNI方法签名，如：(Ljava/lang/String;I)Ljava/lang/String;
     */
    public static String generateSignature(Class<?> returnType, Class<?>... paramTypes) {
        validateTypes(returnType, paramTypes);

        StringBuilder signature = new StringBuilder();
        signature.append('(');

        for (Class<?> param : paramTypes) {
            signature.append(getTypeSignature(param));
        }

        signature.append(')').append(getTypeSignature(returnType));
        return signature.toString();
    }

    /**
     * 获取单个类型的JNI签名
     * @param type 类型
     * @return 类型签名
     */
    private static String getTypeSignature(Class<?> type) {
        // 检查基本类型
        String primitiveSig = PRIMITIVE_SIGNATURES.get(type);
        if (primitiveSig != null) {
            return primitiveSig;
        }

        // 处理数组
        if (type.isArray()) {
            return "[" + getTypeSignature(type.getComponentType());
        }

        // 缓存非数组类类型的签名
        String className = type.getName();
        return CLASS_SIGNATURE_CACHE.computeIfAbsent(className,
                name -> "L" + name.replace('.', '/') + ";"
        );
    }

    /**
     * 验证类型有效性
     */
    private static void validateTypes(Class<?> returnType, Class<?>... paramTypes) {
        if (returnType == null) {
            throw new IllegalArgumentException("Return type cannot be null");
        }

        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == null) {
                throw new IllegalArgumentException(
                        String.format("Parameter type at index %d cannot be null", i)
                );
            }
        }
    }

    /**
     * 便捷方法：生成构造函数签名
     * @param paramTypes 参数类型
     * @return 构造函数签名，如：(Ljava/lang/String;I)V
     */
    public static String generateConstructorSignature(Class<?>... paramTypes) {
        return generateSignature(void.class, paramTypes);
    }

    /**
     * 便捷方法：生成静态方法签名
     * @param methodName 方法名
     * @param returnType 返回类型
     * @param paramTypes 参数类型
     * @return 完整签名
     */
    public static String generateStaticMethodSignature(
            String methodName, Class<?> returnType, Class<?>... paramTypes) {
        return generateSignatureWithMethodName(methodName, returnType, paramTypes);
    }

    /**
     * 清空缓存（主要用于测试）
     */
    public static void clearCache() {
        CLASS_SIGNATURE_CACHE.clear();
    }
}