/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.importer.rest;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.geoserver.importer.ImportContext;
import org.geoserver.importer.ImportTask;
import org.geoserver.importer.Importer;
import org.geoserver.importer.rest.converters.ImportContextJSONConverterReader;
import org.geoserver.importer.rest.converters.ImportContextJSONConverterWriter;
import org.geoserver.importer.transform.ImportTransform;
import org.geoserver.rest.RequestInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.web.context.request.AbstractRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.beans.PropertyDescriptor;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.classextension.EasyMock.createNiceMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 *
 * @author Ian Schneider <ischneider@opengeo.org>
 */
public abstract class TransformTestSupport extends TestCase {

    public void doJSONTest(ImportTransform transform) throws Exception {
        StringWriter buffer = new StringWriter();

        Importer im = createNiceMock(Importer.class);
        RequestInfo ri = createNiceMock(RequestInfo.class);
        
        replay(im, ri);

        RequestContextHolder.setRequestAttributes(new AbstractRequestAttributes() {
            Map<String, Object> requestAttributes = new HashMap<>();

            @Override
            public Object getAttribute(String name, int scope) {
                return requestAttributes.get(name);
            }

            @Override
            public void setAttribute(String name, Object value, int scope) {
                requestAttributes.put(name, value);
            }

            @Override
            public void removeAttribute(String name, int scope) {
                requestAttributes.remove(name);
            }

            @Override
            public String[] getAttributeNames(int scope) {
                return requestAttributes.keySet().toArray(new String[requestAttributes.size()]);
            }

            @Override
            protected void updateAccessedSessionAttributes() { }

            @Override
            public void registerDestructionCallback(String name, Runnable callback, int scope) { }

            @Override
            public Object resolveReference(String key) { return null; }

            @Override
            public String getSessionId() { return null; }

            @Override
            public Object getSessionMutex() { return null; }
        });
        RequestInfo.set(ri);

        ImportContextJSONConverterWriter jsonio = new ImportContextJSONConverterWriter(im, new WriterOutputStream(buffer));

        ImportContext c = new ImportContext(0);
        c.addTask(new ImportTask());

        jsonio.transform(transform, 0, c.task(0), true, 1);

        ImportTransform transform2 = new ImportContextJSONConverterReader(im, IOUtils.toInputStream(buffer.toString())).transform();
        PropertyDescriptor[] pd = BeanUtils.getPropertyDescriptors(transform.getClass());

        for (int i = 0; i < pd.length; i++) {
            assertEquals("expected same value of " + pd[i].getName(),
                    pd[i].getReadMethod().invoke(transform),
                    pd[i].getReadMethod().invoke(transform2));
        }
    }
}
