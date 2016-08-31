package org.camunda.tngp.transport.impl.media;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

import org.camunda.tngp.transport.impl.TransportChannelImpl;
import org.camunda.tngp.transport.impl.agent.Receiver;
import org.camunda.tngp.transport.impl.agent.ReceiverCmd;

import org.agrona.LangUtil;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.agrona.nio.TransportPoller;

public class ReadTransportPoller extends TransportPoller
{
    protected final List<TransportChannelImpl> channels = new ArrayList<>(100);

    protected final ToIntFunction<SelectionKey> processKeyFn = this::processKey;

    protected final ManyToOneConcurrentArrayQueue<ReceiverCmd> cmdQueue;

    public ReadTransportPoller(Receiver receiver)
    {
        this.cmdQueue = receiver.getCmdQueue();
    }

    public int pollNow()
    {
        int workCount = 0;

        if (channels.size() <= ITERATION_THRESHOLD)
        {
            for (int i = 0; i < channels.size(); i++)
            {
                workCount += channels.get(i).receive();
            }
        }
        else
        {
            try
            {
                selector.selectNow();
                workCount = selectedKeySet.forEach(processKeyFn);
            }
            catch (IOException e)
            {
                selectedKeySet.reset();
                LangUtil.rethrowUnchecked(e);
            }
        }

        return workCount;
    }

    protected int processKey(SelectionKey key)
    {
        int workCount = 0;

        if (key != null && key.isReadable())
        {
            final TransportChannelImpl channel = (TransportChannelImpl) key.attachment();
            workCount = channel.receive();
        }

        return workCount;
    }

    public void addChannel(TransportChannelImpl channel)
    {
        channel.registerSelector(selector, SelectionKey.OP_READ);
        channels.add(channel);
    }

    public void removeChannel(TransportChannelImpl channel)
    {
        channel.removeSelector(selector);
        channels.remove(channel);
    }

}
