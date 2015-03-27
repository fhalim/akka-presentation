package net.fawad.persistence

import akka.actor.ActorLogging
import akka.persistence.PersistentActor

class Barista(id: String) extends PersistentActor with ActorLogging {
  override def persistenceId = id

  var balance = 0

  override def receiveCommand = {
    case OrderCoffee(order) =>
      log.info("Coffee ordered: {}", order)
      self ! CoffeeOrdered(sender, order)
    case PayForCoffee(order, amount) =>
      persistAsync(PaidForCoffee(sender, order, amount))(self ! _)
    case CoffeeOrdered(customer, order) =>
      log.info("Making coffee!")
      Thread.sleep(100)
      self ! CoffeeMade(customer, order)
    case CoffeeMade(customer, order) =>
      customer ! GiveCoffee(order)
    case evt@PaidForCoffee(customer, order, amount) =>
      log.info("Customer paid ${} for coffee.", amount, balance)
      updateState(evt)
      customer ! "Thank you"
  }

  override def receiveRecover = {
    case evt@PaidForCoffee(customer, order, amount) => updateState(evt)
  }

  def updateState(evt: PaidForCoffee) = evt match {
    case PaidForCoffee(customer, order, amount) =>
      balance += amount
      log.info("Balance is {}", balance)
  }
}
