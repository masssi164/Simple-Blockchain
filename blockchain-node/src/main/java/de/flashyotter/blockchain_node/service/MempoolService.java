package de.flashyotter.blockchain_node.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("#{ @nodeProperties.isFull() }")
public class MempoolService { … }
