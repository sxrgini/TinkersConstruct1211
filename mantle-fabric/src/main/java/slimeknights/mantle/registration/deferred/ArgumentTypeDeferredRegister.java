package slimeknights.mantle.registration.deferred;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.platform.registry.DeferredHolder;
import slimeknights.mantle.platform.registry.DeferredRegister;
import slimeknights.mantle.registration.RegistrationHelper;

import java.util.function.Supplier;

/** Register for argument types that automatically handles registering with {@link ArgumentTypeInfos#registerByClass(Class, ArgumentTypeInfo)} */
@SuppressWarnings("unused")  // API
public class ArgumentTypeDeferredRegister extends DeferredRegister<ArgumentTypeInfo<?,?>> {
  public ArgumentTypeDeferredRegister(String modID) {
    super(Registries.COMMAND_ARGUMENT_TYPE, modID);
  }

  /**
   * Registers an argument type
   * @param name           Name of the argument
   * @param argumentClass  Class of the argument
   * @param supplier       Supplier to the argument info
   * @param <A>  Argument type
   * @param <T>  Argument info template type
   * @param <I>  Argument info type
   * @return  Registry object
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <A extends ArgumentType<?>,T extends ArgumentTypeInfo.Template<A>,I extends ArgumentTypeInfo<A,T>> DeferredHolder<ArgumentTypeInfo<?,?>,I> register(String name, Class<? super A> argumentClass, Supplier<I> supplier) {
    // Fabric registers the type in the registry and the class map in one call, so this registers immediately rather than queueing
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(getNamespace(), name);
    I info = supplier.get();
    net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry.registerArgumentType(id, RegistrationHelper.genericArgumentType(argumentClass), (ArgumentTypeInfo) info);
    return DeferredHolder.create(Registries.COMMAND_ARGUMENT_TYPE, id);
  }

  /**
   * Registers a context free singleton argument
   * @param name           Name of the argument
   * @param argumentClass  Class of the argument
   * @param supplier       Supplier to the argument default
   * @param <A>  Argument type
   * @return  Registry object
   */
  public <A extends ArgumentType<?>> DeferredHolder<ArgumentTypeInfo<?,?>,SingletonArgumentInfo<A>> registerSingleton(String name, Class<A> argumentClass, Supplier<A> supplier) {
    return register(name, argumentClass, () -> SingletonArgumentInfo.contextFree(supplier));
  }
}
