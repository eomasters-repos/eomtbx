package org.eomasters;

import org.openide.modules.OnStart;

@OnStart
public class OnStartAction implements Runnable {
  @Override
  public void run() {
    System.out.println();
  }
}
