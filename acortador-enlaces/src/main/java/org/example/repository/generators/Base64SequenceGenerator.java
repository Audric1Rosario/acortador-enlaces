package org.example.repository.generators;


import org.example.util.CustomBase64;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.io.*;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.hibernate.MappingException;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.BigIntegerType;
import org.hibernate.type.Type;

//esto generan los ids automaticamente dado un bigInteger
public class Base64SequenceGenerator extends SequenceStyleGenerator {
    public static final String RESERVED_LIST_FILENAME = "reserved_list_filename";
    public static final String DEFAULT_RESERVED_LIST_FILENAME = null;
    public static final String RESERVED_CASE_SENSITIVE = "reserved_case_sensitive";
    public static final boolean DEFAULT_RESERVED_CASE_SENSITIVE = false;


    private Set<String> reserved;
    private boolean reserved_case_sensitive;

    @Override
    public Serializable generate(SharedSessionContractImplementor session,
                                 Object object) throws HibernateException {
        String next = CustomBase64.encode((BigInteger) super.generate(session, object));
        if(reserved_case_sensitive) {
            while (reserved.contains(next.toLowerCase())) {
                next = CustomBase64.encode((BigInteger) super.generate(session, object));
            }
        } else {
            while (reserved.contains(next)) {
                next = CustomBase64.encode((BigInteger) super.generate(session, object));
            }
        }
        return next;
    }

    @Override
    public void configure(Type type, Properties params,
                          ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(BigIntegerType.INSTANCE, params, serviceRegistry);

        String reservedListFileName = ConfigurationHelper.getString(RESERVED_LIST_FILENAME,
                params, DEFAULT_RESERVED_LIST_FILENAME);

        this.reserved_case_sensitive = ConfigurationHelper.getBoolean(RESERVED_CASE_SENSITIVE,
                params, DEFAULT_RESERVED_CASE_SENSITIVE);
        this.reserved = new HashSet<>();

        if (reservedListFileName != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Base64SequenceGenerator.class.getClassLoader().getResourceAsStream(reservedListFileName)));
            try {
                while (reader.ready()) {
                    String line = reader.readLine();
                    reserved.add(this.reserved_case_sensitive?line.toLowerCase():line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}