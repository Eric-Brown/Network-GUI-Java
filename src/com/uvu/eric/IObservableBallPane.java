package com.uvu.eric;

public interface IObservableBallPane
{
    public void RegisterObserver(IBallPaneObserver observer);
    public void UnregisterObserver(IBallPaneObserver observer);
    public void NotifyObservers();
}
