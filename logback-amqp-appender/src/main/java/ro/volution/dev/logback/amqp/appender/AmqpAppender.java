
package ro.volution.dev.logback.amqp.appender;


import java.io.Serializable;
import java.util.concurrent.LinkedBlockingDeque;

import ro.volution.dev.logback.amqp.common.DefaultMutator;
import ro.volution.dev.logback.amqp.common.Mutator;

import ro.volution.dev.logback.amqp.common.Callbacks;
import ro.volution.dev.logback.amqp.common.DefaultBinarySerializer;
import ro.volution.dev.logback.amqp.common.DefaultContextAwareCallbacks;
import ro.volution.dev.logback.amqp.common.SerializableLoggingEvent1;
import ro.volution.dev.logback.amqp.common.Serializer;

import ro.volution.dev.logback.amqp.accessors.AmqpMessage;
import ro.volution.dev.logback.amqp.accessors.AmqpPublisher;


import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.UnsynchronizedAppenderBase;


public class AmqpAppender
		extends UnsynchronizedAppenderBase<ILoggingEvent>
{
	public AmqpAppender ()
	{
		super ();
		this.callbacks = new DefaultContextAwareCallbacks (this);
		this.buffer = new LinkedBlockingDeque<AmqpMessage> ();
		this.exchangeLayout = new PatternLayout ();
		this.routingKeyLayout = new PatternLayout ();
		this.exchangeLayout.setPattern (AmqpAppender.defaultExchangeKeyPattern);
		this.routingKeyLayout.setPattern (AmqpAppender.defaultRoutingKeyPattern);
		this.mutator = new DefaultMutator ();
		this.serializer = new DefaultBinarySerializer ();
		this.publisher = null;
	}
	
	public final String getExchangePattern ()
	{
		return (this.exchangeLayout.getPattern ());
	}
	
	public final String getHost ()
	{
		return (this.host);
	}
	
	public final Mutator getMutator ()
	{
		return (this.mutator);
	}
	
	public final String getPassword ()
	{
		return (this.password);
	}
	
	public final Integer getPort ()
	{
		return (this.port);
	}
	
	public final String getRoutingKeyPattern ()
	{
		return (this.routingKeyLayout.getPattern ());
	}
	
	public final Serializer getSerializer ()
	{
		return (this.serializer);
	}
	
	public final String getUsername ()
	{
		return (this.username);
	}
	
	public final String getVirtualHost ()
	{
		return (this.virtualHost);
	}
	
	public final boolean isDrained ()
	{
		return (this.buffer.isEmpty ());
	}
	
	public final boolean isRunning ()
	{
		final AmqpPublisher publisher = this.publisher;
		return (((publisher != null) && publisher.isRunning ()) || super.isStarted ());
	}
	
	public final void setContext (final Context context)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		super.setContext (context);
		this.exchangeLayout.setContext (context);
		this.routingKeyLayout.setContext (context);
	}
	
	public final void setExchangePattern (final String pattern)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		this.exchangeLayout.setPattern (pattern);
	}
	
	public final void setHost (final String host)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		this.host = host;
	}
	
	public final void setMutator (final Mutator mutator)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		this.mutator = mutator;
	}
	
	public final void setPassword (final String password)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		this.password = password;
	}
	
	public final void setPort (final Integer port)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		this.port = port;
	}
	
	public final void setRoutingKeyPattern (final String pattern)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		this.routingKeyLayout.setPattern (pattern);
	}
	
	public final void setSerializer (final Serializer serializer)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		this.serializer = serializer;
	}
	
	public final void setUsername (final String username)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		this.username = username;
	}
	
	public final void setVirtualHost (final String virtualHost)
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		this.virtualHost = virtualHost;
	}
	
	public final void start ()
	{
		if (this.isStarted ())
			throw (new IllegalStateException ("amqp appender is already started"));
		if (this.publisher != null)
			throw (new IllegalStateException ("amqp appender has passed its life-cycle"));
		this.preStart ();
		this.exchangeLayout.start ();
		this.routingKeyLayout.start ();
		this.publisher =
				new AmqpPublisher (
						this.host, this.port, this.virtualHost, this.username, this.password, this.callbacks, this.buffer);
		this.publisher.start ();
		super.start ();
		this.postStart ();
	}
	
	public final void stop ()
	{
		if (!this.isStarted ())
			throw (new IllegalStateException ("amqp appender is not started"));
		this.preStop ();
		this.exchangeLayout.stop ();
		this.routingKeyLayout.stop ();
		this.publisher.stop ();
		super.stop ();
		this.postStop ();
	}
	
	protected final void append (final ILoggingEvent originalEvent)
	{
		final Serializable event = this.prepare (originalEvent);
		byte[] data;
		try {
			data = this.serializer.serialize (event);
		} catch (final Throwable exception) {
			data = null;
			this.addError ("amqp appender encountered an error while serializing the event; ignoring!", exception);
		}
		if (data != null) {
			final String exchange = this.exchangeLayout.doLayout (originalEvent);
			final String routingKey = this.routingKeyLayout.doLayout (originalEvent);
			final AmqpMessage message =
					new AmqpMessage (
							exchange, routingKey, this.serializer.getContentType (), this.serializer.getContentEncoding (),
							data);
			this.buffer.add (message);
		}
	}
	
	protected void postStart ()
	{}
	
	protected void postStop ()
	{}
	
	protected void preStart ()
	{}
	
	protected void preStop ()
	{}
	
	private final Serializable prepare (final ILoggingEvent originalEvent)
	{
		final SerializableLoggingEvent1 newEvent = SerializableLoggingEvent1.build (originalEvent);
		if (this.mutator != null)
			this.mutator.mutate (newEvent);
		return (newEvent);
	}
	
	protected final Callbacks callbacks;
	private final LinkedBlockingDeque<AmqpMessage> buffer;
	private final PatternLayout exchangeLayout;
	private String host;
	private Mutator mutator;
	private String password;
	private Integer port;
	private AmqpPublisher publisher;
	private final PatternLayout routingKeyLayout;
	private Serializer serializer;
	private String username;
	private String virtualHost;
	
	public static final String defaultExchangeKeyPattern = "logback%nopex";
	public static final String defaultRoutingKeyPattern = "%level%nopex";
}