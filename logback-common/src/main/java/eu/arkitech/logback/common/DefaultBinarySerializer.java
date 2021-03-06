
package eu.arkitech.logback.common;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;


public class DefaultBinarySerializer
		implements
			Serializer
{
	public DefaultBinarySerializer ()
	{
		this (
				DefaultBinarySerializer.defaultContentType, DefaultBinarySerializer.defaultContentEncoding,
				DefaultBinarySerializer.defaultBufferSize);
	}
	
	public DefaultBinarySerializer (final String contentType, final String contentEncoding, final int bufferSize)
	{
		super ();
		this.contentType = contentType;
		this.contentEncoding = contentEncoding;
		this.bufferSize = bufferSize;
	}
	
	public Object deserialize (final byte[] data)
			throws Throwable
	{
		return (this.deserialize (data, 0, data.length));
	}
	
	public Object deserialize (final byte[] data, final int offset, final int size)
			throws Throwable
	{
		final ByteArrayInputStream stream = new ByteArrayInputStream (data, offset, size);
		final InputStream decoratedStream = this.decorate (stream);
		final ObjectInputStream decoder = new ObjectInputStream (decoratedStream);
		final Object object = decoder.readObject ();
		decoder.close ();
		return (object);
	}
	
	public int getBufferSize ()
	{
		return (this.bufferSize);
	}
	
	public String getContentEncoding ()
	{
		return (this.contentEncoding);
	}
	
	public String getContentType ()
	{
		return (this.contentType);
	}
	
	public byte[] serialize (final Object object)
			throws Throwable
	{
		final ByteArrayOutputStream stream = new ByteArrayOutputStream (this.bufferSize);
		final OutputStream decoratedStream = this.decorate (stream);
		final ObjectOutputStream encoder = new ObjectOutputStream (decoratedStream);
		encoder.writeObject (object);
		encoder.close ();
		return (stream.toByteArray ());
	}
	
	public void setBufferSize (final int bufferSize)
	{
		this.bufferSize = bufferSize;
	}
	
	public void setContentEncoding (final String contentEncoding)
	{
		this.contentEncoding = contentEncoding;
	}
	
	public void setContentType (final String contentType)
	{
		this.contentType = contentType;
	}
	
	@SuppressWarnings ("unused")
	protected InputStream decorate (final InputStream stream)
			throws Throwable
	{
		return (stream);
	}
	
	@SuppressWarnings ("unused")
	protected OutputStream decorate (final OutputStream stream)
			throws Throwable
	{
		return (stream);
	}
	
	protected int bufferSize;
	protected String contentEncoding;
	protected String contentType;
	
	public static final int defaultBufferSize = 2048;
	public static final String defaultContentEncoding = "binary";
	public static final String defaultContentType = "application/x-java-serialized-object";
}
