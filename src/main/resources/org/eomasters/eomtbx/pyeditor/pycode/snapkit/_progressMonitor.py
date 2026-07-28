"""
Progress Monitor for tracking long-running operations.

This module provides a Python implementation of the Java ProgressMonitor interface
for use with Graalpython. It allows Python code to implement progress monitoring
functionality that can be used by Java code or to mirror the behavior of Java's
ProgressMonitor interface.

The ProgressMonitor interface is designed for tracking progress of long-running
operations, providing methods to report task progress, handle cancellation,
and manage subtasks.

Key Features:
    - Task progress tracking with begin/end lifecycle
    - Work unit reporting (both known and unknown amounts)
    - Cancellation support for long-running operations
    - Subtask management for nested operations
    - Thread-safe implementation for concurrent usage

Example:
    Basic progress monitoring:

        >>> from snapkit.ProgressMonitor import ProgressMonitor
        >>> 
        >>> # Create a progress monitor
        >>> pm = ProgressMonitor()
        >>> 
        >>> # Begin a task with known work units
        >>> pm.beginTask("Processing data", 100)
        >>> 
        >>> # Report progress
        >>> for i in range(10):
        >>>     # Do some work
        >>>     pm.worked(10)
        >>>     if pm.isCanceled():
        >>>         break
        >>> 
        >>> # Complete the task
        >>> pm.done()

    Working with subtasks:

        >>> pm = ProgressMonitor()
        >>> pm.beginTask("Main processing", ProgressMonitor.UNKNOWN)
        >>> 
        >>> pm.setSubTaskName("Loading data...")
        >>> # Do loading work
        >>> pm.worked(1)
        >>> 
        >>> pm.setSubTaskName("Processing data...")
        >>> # Do processing work
        >>> pm.worked(1)
        >>> 
        >>> pm.done()

    Cancellation handling:

        >>> pm = ProgressMonitor()
        >>> pm.beginTask("Long operation", 1000)
        >>> 
        >>> for i in range(1000):
        >>>     if pm.isCanceled():
        >>>         print("Operation was cancelled")
        >>>         break
        >>>     # Do work
        >>>     pm.worked(1)
        >>> 
        >>> pm.done()
"""


class ProgressMonitor:
    """Progress Monitor for tracking long-running operations.
    
    This class provides a Python implementation of the Java ProgressMonitor interface,
    allowing progress tracking, cancellation support, and subtask management for
    long-running operations in a Graalpython environment.
    """

    # Constant indicating an unknown amount of work
    UNKNOWN = -1

    def __init__(self):
        """Initialize a new ProgressMonitor instance.
        
        Creates a new progress monitor with default state (not started, not cancelled).
        """
        self._task_name = None
        self._sub_task_name = None
        self._total_work = 0
        self._worked_so_far = 0.0
        self._cancelled = False
        self._started = False

    def beginTask(self, taskName: str, totalWork: int):
        """Notifies that the main task is beginning.
        
        This must only be called once on a given progress monitor instance.
        
        Args:
            taskName (str): The name (or description) of the main task
            totalWork (int): The total number of work units into which the main task
                           is been subdivided. If the value is UNKNOWN the implementation
                           is free to indicate progress in a way which doesn't require
                           the total number of work units in advance.
                           
        Example:
            >>> pm = ProgressMonitor()
            >>> pm.beginTask("Processing files", 100)
            >>> # or for unknown amount of work
            >>> pm.beginTask("Loading data", ProgressMonitor.UNKNOWN)
        """
        if self._started:
            raise RuntimeError("beginTask() has already been called on this progress monitor")
        
        self._task_name = taskName
        self._total_work = totalWork
        self._worked_so_far = 0.0
        self._started = True

    def done(self):
        """Notifies that the work is done.
        
        That is, either the main task is completed or the user canceled it. 
        This method may be called more than once (implementations should be 
        prepared to handle this case).
        
        Example:
            >>> pm = ProgressMonitor()
            >>> pm.beginTask("Task", 10)
            >>> # do work...
            >>> pm.done()  # Mark as complete
        """
        # Implementation can be called multiple times safely
        self._started = False

    def internalWorked(self, work: float):
        """Internal method to handle scaling correctly.
        
        This method must not be called by a client. Clients should
        always use the method worked(int).
        
        Args:
            work (float): The amount of work done
            
        Note:
            This is an internal method used by the progress monitoring system
            for precise work tracking with floating-point values.
        """
        if self._started:
            self._worked_so_far += work

    def isCanceled(self) -> bool:
        """Returns whether cancelation of current operation has been requested.
        
        Long-running operations should poll to see if cancelation has been requested.
        
        Returns:
            bool: True if cancellation has been requested, False otherwise
            
        Example:
            >>> pm = ProgressMonitor()
            >>> pm.beginTask("Long task", 1000)
            >>> for i in range(1000):
            >>>     if pm.isCanceled():
            >>>         print("User cancelled the operation")
            >>>         break
            >>>     # do work
            >>>     pm.worked(1)
        """
        return self._cancelled

    def setCanceled(self, canceled: bool) :
        """Sets the cancel state to the given value.
        
        Args:
            canceled (bool): True indicates that cancelation has been requested
                           (but not necessarily acknowledged); False clears this flag
                           
        Example:
            >>> pm = ProgressMonitor()
            >>> pm.setCanceled(True)  # Request cancellation
            >>> if pm.isCanceled():
            >>>     print("Cancellation requested")
        """
        self._cancelled = canceled

    def setTaskName(self, taskName: str):
        """Sets the task name to the given value.
        
        This method is used to restore the task label after a nested operation
        was executed. Normally there is no need for clients to call this method.
        
        Args:
            taskName (str): The name (or description) of the main task
            
        Example:
            >>> pm = ProgressMonitor()
            >>> pm.beginTask("Original task", 100)
            >>> pm.setTaskName("Updated task name")
        """
        self._task_name = taskName

    def setSubTaskName(self, subTaskName: str):
        """Notifies that a subtask of the main task is beginning.
        
        Subtasks are optional; the main task might not have subtasks.
        
        Args:
            subTaskName (str): The name (or description) of the subtask
            
        Example:
            >>> pm = ProgressMonitor()
            >>> pm.beginTask("Main processing", ProgressMonitor.UNKNOWN)
            >>> pm.setSubTaskName("Loading configuration...")
            >>> # do loading work
            >>> pm.setSubTaskName("Processing data...")
            >>> # do processing work
        """
        self._sub_task_name = subTaskName

    def worked(self, work: int):
        """Notifies that a given number of work unit of the main task has been completed.
        
        Note that this amount represents an installment, as opposed to a cumulative
        amount of work done to date.
        
        Args:
            work (int): The number of work units just completed
            
        Example:
            >>> pm = ProgressMonitor()
            >>> pm.beginTask("Processing items", 100)
            >>> for i in range(10):
            >>>     # Process 10 items at a time
            >>>     process_items(10)
            >>>     pm.worked(10)  # Report 10 units completed
        """
        if work > 0:
            self.internalWorked(float(work))

    # Properties for accessing current state (Python convenience methods)
    @property
    def task_name(self) -> str:
        """Get the current task name."""
        return self._task_name

    @property
    def sub_task_name(self) -> str:
        """Get the current subtask name."""
        return self._sub_task_name

    @property
    def total_work(self) -> int:
        """Get the total work units for this task."""
        return self._total_work

    @property
    def worked_so_far(self) -> float:
        """Get the amount of work completed so far."""
        return self._worked_so_far

    def __repr__(self) -> str:
        """Return string representation of the ProgressMonitor."""
        status = "cancelled" if self._cancelled else ("running" if self._started else "not started")
        return f"ProgressMonitor(task='{self._task_name}', status={status}, progress={self._worked_so_far}/{self._total_work})"


class NullProgressMonitor(ProgressMonitor):
    """A progress monitor implementation that ignores all calls.
    
    This is a null object implementation that can be used when progress
    monitoring is not needed but a ProgressMonitor interface is required.
    All methods are implemented as no-ops.
    """

    def beginTask(self, taskName: str, totalWork: int):
        """No-op implementation of beginTask."""
        pass

    def done(self):
        """No-op implementation of done."""
        pass

    def internalWorked(self, work: float):
        """No-op implementation of internalWorked."""
        pass

    def isCanceled(self) -> bool:
        """Always returns False for null monitor."""
        return False

    def setCanceled(self, canceled: bool):
        """No-op implementation of setCanceled."""
        pass

    def setTaskName(self, taskName: str):
        """No-op implementation of setTaskName."""
        pass

    def setSubTaskName(self, subTaskName: str):
        """No-op implementation of setSubTaskName."""
        pass

    def worked(self, work: int):
        """No-op implementation of worked."""
        pass

    def __repr__(self) -> str:
        """Return string representation of the NullProgressMonitor."""
        return "NullProgressMonitor()"


class CallbackProgressMonitor(ProgressMonitor):
    def __init__(self, callback, callback_interval=10):
        super().__init__()
        self.callback = callback
        self.old_progress = 0
        self.notification_rate = callback_interval

    def worked(self, work: int):
        super().worked(work)

        progress = (self.worked_so_far * 100) / self.total_work
        # print(f"work: {work}; worked so far: {self.worked_so_far}; total work: {self.total_work}; progress: {progress}")
        delta_progress = progress - self.old_progress
        if delta_progress >= self.notification_rate:
            self.callback(round(progress, 2))
            self.old_progress = progress