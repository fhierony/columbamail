package org.columba.core.command;

import org.columba.api.command.IWorkerStatusController;

public class TestCommand extends Command {
  private int id;

  private DefaultProcessorTest test;

  public TestCommand(DefaultProcessorTest test, int id) {
    this(test, id, Command.NORMAL_PRIORITY);
  }
  
  public TestCommand() {
    super(null);
    // nothing to do
  }

  public TestCommand(DefaultProcessorTest test, int id, int priority) {
    super(null);
    this.id = id;
    this.test = test;
    this.priority = priority;
  }

  public void updateGUI() {
    test.finishedID = id;
  }

  public void execute(IWorkerStatusController worker) throws Exception {
    test.executedID = id;
    Thread.sleep(50);
  }

  /**
   * @return Returns the id.
   */
  public int getId() {
    return id;
  }

  /**
   * @param id
   *            The id to set.
   */
  public void setId(int id) {
    this.id = id;
  }
}