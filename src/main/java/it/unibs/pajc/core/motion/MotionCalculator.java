package it.unibs.pajc.core.motion;

import it.unibs.pajc.core.Particle;
import it.unibs.pajc.core.ParticleState;

import java.util.Map;

public interface MotionCalculator {

    ParticleState calculate(Particle particle, double deltaTime, double elapsedTime);

    Map<String, Double> getParameters();

    void reset();

    MotionType getMotionType();
}
