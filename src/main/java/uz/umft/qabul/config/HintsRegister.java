package uz.umft.qabul.config;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.UUID;

public class HintsRegister implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
            hints.reflection().registerType(
                    UUID[].class,
                    builder -> builder.withMembers(
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                            MemberCategory.INVOKE_DECLARED_METHODS,
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.ACCESS_DECLARED_FIELDS,
                            MemberCategory.ACCESS_PUBLIC_FIELDS,
                            MemberCategory.UNSAFE_ALLOCATED
                    )
            );
    }
}
