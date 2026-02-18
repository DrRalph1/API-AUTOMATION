package com.usg.apiAutomation.utils;

import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Component
public class ModelMapperUtil {

    /**
     * -- GETTER --
     *  Get the underlying ModelMapper instance
     *
     * @return ModelMapper instance
     */
    private final ModelMapper modelMapper;

    // No @Autowired needed for default constructor
    public ModelMapperUtil() {
        this.modelMapper = new ModelMapper();
        configureModelMapper();
    }

    /**
     * Configure ModelMapper with custom settings
     */
    private void configureModelMapper() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true)
                .setDeepCopyEnabled(false);
    }

    /**
     * Map source object to destination type
     * @param source Source object
     * @param destinationType Destination class type
     * @return Mapped object of destination type
     */
    public <D, T> D map(final T source, Class<D> destinationType) {
        if (source == null) {
            return null;
        }
        return modelMapper.map(source, destinationType);
    }

    /**
     * Map source object to destination object
     * @param source Source object
     * @param destination Destination object
     * @return Mapped destination object
     */
    public <S, D> D map(final S source, D destination) {
        if (source == null || destination == null) {
            return destination;
        }
        modelMapper.map(source, destination);
        return destination;
    }

    /**
     * Map collection of source objects to list of destination type
     * @param sourceList Collection of source objects
     * @param destinationType Destination class type
     * @return List of mapped objects
     */
    public <D, T> List<D> mapAll(final Collection<T> sourceList, Class<D> destinationType) {
        if (sourceList == null) {
            return null;
        }
        return sourceList.stream()
                .map(source -> map(source, destinationType))
                .collect(Collectors.toList());
    }

    /**
     * Map source object to destination type using Type
     * @param source Source object
     * @param type Type of destination
     * @return Mapped object
     */
    public <D> D map(final Object source, Type type) {
        if (source == null) {
            return null;
        }
        return modelMapper.map(source, type);
    }

    /**
     * Add custom type mapping
     * @param sourceType Source type class
     * @param destinationType Destination type class
     */
    public <S, D> void addMapping(Class<S> sourceType, Class<D> destinationType) {
        modelMapper.createTypeMap(sourceType, destinationType);
    }

    /**
     * Validate mapping between two classes
     * @param sourceType Source type class
     * @param destinationType Destination type class
     * @return true if mapping is valid
     */
    public <S, D> boolean validateMapping(Class<S> sourceType, Class<D> destinationType) {
        try {
            modelMapper.createTypeMap(sourceType, destinationType).validate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}