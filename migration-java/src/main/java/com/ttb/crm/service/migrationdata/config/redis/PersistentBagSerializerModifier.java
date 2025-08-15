package com.ttb.crm.service.migrationdata.config.redis;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Persistence;
import org.hibernate.collection.spi.PersistentBag;

import java.util.ArrayList;
import java.util.List;

public class PersistentBagSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        return beanProperties.stream()
                .map(writer -> {
                    if (!isJPARelationField(writer)) {
                        return writer;
                    }
                    return new BeanPropertyWriter(writer) {
                        @Override
                        public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov)
                                throws Exception {
                            Object value = get(bean);
                            if (value == null) {
                                super.serializeAsField(bean, gen, prov);
                                return;
                            }
                            if (Persistence.getPersistenceUtil().isLoaded(value)) {
                                Object sanitized = sanitizeIfPersistentBag(value);
                                gen.writeFieldName(getName());
                                prov.defaultSerializeValue(sanitized, gen);
                            }
                        }
                    };
                })
                .toList();
    }

    private boolean isJPARelationField(BeanPropertyWriter writer) {
        AnnotatedMember member = writer.getMember();
        return member.hasAnnotation(ManyToOne.class)
                || member.hasAnnotation(OneToMany.class)
                || member.hasAnnotation(OneToOne.class)
                || member.hasAnnotation(ManyToMany.class);
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeIfPersistentBag(Object value) {
        if (value instanceof PersistentBag bag) {
            return new ArrayList<>(bag);
        }
        return value;
    }
}