package net.fawad.persistence

import akka.actor.ActorLogging
import akka.agent.Agent
import akka.persistence.PersistentActor

class CashRegister extends PersistentActor with ActorLogging {
  override def persistenceId = "cashregister"

  implicit val ec = context.dispatcher

  val balance = Agent(0)
  override def receiveCommand = {
    case AddMoney(amount) =>
      persistAsync(MoneyAdded(amount))(self ! _)
    case evt@MoneyAdded(amount) =>
      log.info("Added ${}", amount)
      updateState(evt)
      printState()
  }

  override def receiveRecover = {
    case evt:MoneyAdded => updateState(evt)
  }

  def printState() = {
    for (result <- balance.future()) {
      log.info("Now have ${} in register", result)
    }
  }

  def updateState(evt: MoneyAdded) {
      balance send (_ + evt.amount)
  }

}
