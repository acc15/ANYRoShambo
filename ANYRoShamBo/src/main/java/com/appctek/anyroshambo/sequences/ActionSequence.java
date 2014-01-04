package com.appctek.anyroshambo.sequences;

/**
* @author Vyacheslav Mayorov
* @since 2014-04-01
*/
public interface ActionSequence {
    LazyAction executeStep(int step, Sequencer sequencer);
}
