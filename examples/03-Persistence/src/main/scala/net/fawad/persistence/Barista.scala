package net.fawad.persistence

import akka.actor.{Actor, ActorLogging}


class Barista extends Actor with ActorLogging {
  override def receive = {
    case OrderCoffee(order) =>
      log.info("Coffee ordered: {}", order)
      context.actorSelection("/user/barista") ! CoffeeOrdered(sender, order)
      takeABreak()
    case PayForCoffee(order, amount) =>
      context.actorSelection("/user/cashregister") ! AddMoney(amount)
      sender ! "Thank you"
    case CoffeeOrdered(customer, order) =>
      log.info("Making coffee {}!", order)
      context.actorSelection("/user/barista") ! CoffeeMade(customer, order)
      takeABreak()
    case CoffeeMade(customer, order) =>
      customer ! GiveCoffee(order)
      takeABreak()
  }
  def takeABreak(): Unit ={
    Thread.sleep(10)
  }
}
