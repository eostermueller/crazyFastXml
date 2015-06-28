package com.github.eostermueller.keyedpool;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class StringBufferFactory
    extends BaseKeyedPooledObjectFactory<String, StringBuffer> {

    /**
     * Use the default PooledObject implementation.
     */
    @Override
    public PooledObject<StringBuffer> wrap(StringBuffer buffer) {
        return new DefaultPooledObject<StringBuffer>(buffer);
    }

    /**
     * When an object is returned to the pool, clear the buffer.
     */
    @Override
    public void passivateObject(String key, PooledObject<StringBuffer> pooledObject) {
        pooledObject.getObject().setLength(0);
    }

	@Override
	public StringBuffer create(String key) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

    // for all other methods, the no-op implementation
    // in BasePooledObjectFactory will suffice
}
