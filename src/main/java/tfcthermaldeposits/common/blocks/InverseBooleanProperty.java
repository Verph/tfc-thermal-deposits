package tfcthermaldeposits.common.blocks;

import java.util.Collection;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;

import net.minecraft.world.level.block.state.properties.Property;

public class InverseBooleanProperty extends Property<Boolean>
{
    private final ImmutableSet<Boolean> values = ImmutableSet.of(false, true);

    protected InverseBooleanProperty(String name)
    {
        super(name, Boolean.class);
    }

    @Override
    public Collection<Boolean> getPossibleValues()
    {
        return this.values;
    }

    public static InverseBooleanProperty create(String name)
    {
        return new InverseBooleanProperty(name);
    }

    @Override
    public Optional<Boolean> getValue(String value)
    {
        return !"true".equals(value) && !"false".equals(value) ? Optional.empty() : Optional.of(Boolean.valueOf(value));
    }

    @Override
    public String getName(Boolean value)
    {
        return value.toString();
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other instanceof InverseBooleanProperty isOther && super.equals(other))
        {
            InverseBooleanProperty property = isOther;
            return this.values.equals(property.values);
        }
        else
        {
            return false;
        }
    }

    @Override
    public int generateHashCode()
    {
        return 31 * super.generateHashCode() + this.values.hashCode();
    }
}