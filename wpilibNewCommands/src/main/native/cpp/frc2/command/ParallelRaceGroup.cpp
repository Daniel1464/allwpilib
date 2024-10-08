// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

#include "frc2/command/ParallelRaceGroup.h"

#include <string>
#include <utility>
#include <vector>

using namespace frc2;

ParallelRaceGroup::ParallelRaceGroup(
    std::vector<std::unique_ptr<Command>>&& commands) {
  AddCommands(std::move(commands));
}

void ParallelRaceGroup::Initialize() {
  m_finished = false;
  for (auto& commandRunning : m_commands) {
    commandRunning->Initialize();
  }
  isRunning = true;
}

void ParallelRaceGroup::Execute() {
  for (auto& commandRunning : m_commands) {
    commandRunning->Execute();
    if (commandRunning->IsFinished()) {
      m_finished = true;
    }
  }
}

void ParallelRaceGroup::End(bool interrupted) {
  for (auto& commandRunning : m_commands) {
    commandRunning->End(!commandRunning->IsFinished());
  }
  isRunning = false;
}

bool ParallelRaceGroup::IsFinished() {
  return m_finished;
}

bool ParallelRaceGroup::RunsWhenDisabled() const {
  return m_runWhenDisabled;
}

Command::InterruptionBehavior ParallelRaceGroup::GetInterruptionBehavior()
    const {
  return m_interruptBehavior;
}

void ParallelRaceGroup::AddCommands(
    std::vector<std::unique_ptr<Command>>&& commands) {
  CommandScheduler::GetInstance().RequireUngroupedAndUnscheduled(commands);

  if (isRunning) {
    throw FRC_MakeError(frc::err::CommandIllegalUse,
                        "Commands cannot be added to a CommandGroup "
                        "while the group is running");
  }

  for (auto&& command : commands) {
    auto sharedRequirements = GetSharedRequirements(this, command.get());
    if (sharedRequirements.empty()) {
      command->SetComposed(true);
      AddRequirements(command->GetRequirements());
      m_runWhenDisabled &= command->RunsWhenDisabled();
      if (command->GetInterruptionBehavior() ==
          Command::InterruptionBehavior::kCancelSelf) {
        m_interruptBehavior = Command::InterruptionBehavior::kCancelSelf;
      }
      m_commands.emplace_back(std::move(command));
    } else {
      std::string formattedRequirements = "";
      bool first = true;
      for (auto&& requirement : sharedRequirements) {
        if (first) {
          first = false;
        } else {
          formattedRequirements += ", ";
        }
        formattedRequirements += requirement->GetName();
      }
      throw FRC_MakeError(
          frc::err::CommandIllegalUse,
          "Command {} could not be added to this ParallelCommandGroup"
          " because the subsystems [{}] are already required in this command."
          " Multiple commands in a parallel composition cannot require the "
          "same subsystems.",
          command->GetName(), formattedRequirements);
    }
  }
}
