package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.SlateNavyDark
import com.example.ui.viewmodel.EcommerceViewModel

@Composable
fun SupportScreen(
    prefilledOrderId: String? = null,
    viewModel: EcommerceViewModel
) {
    var selectedTab by remember { mutableIntStateOf(if (prefilledOrderId != null) 1 else 0) }
    val chatMessages by viewModel.chatMessages.collectAsState()
    val tickets by viewModel.supportTickets.collectAsState()

    var chatInput by remember { mutableStateOf("") }

    // Ticket form state
    var ticketOrderId by remember { mutableStateOf(prefilledOrderId ?: "") }
    var ticketSubject by remember { mutableStateOf("") }
    var ticketCategory by remember { mutableStateOf("Delivery Delay") }
    var ticketMessage by remember { mutableStateOf("") }
    var ticketCreatedNotice by remember { mutableStateOf<String?>(null) }

    val quickQuestions = listOf(
        "Where is my package?",
        "How do refunds work?",
        "What currencies are supported?",
        "How to contact my vendor?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = CoralPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Customer Support Desk",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "24/7 Global assistance & 3PL order resolution",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tabs: Live Chat vs Support Tickets
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = CoralPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Live Support Chat", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Tickets (${tickets.size})", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // Live Chat View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Quick Questions Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(quickQuestions) { q ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.clickable {
                                viewModel.sendChatMessage(q)
                            }
                        ) {
                            Text(
                                text = q,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Messages Stream
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isUser) 14.dp else 2.dp,
                                            bottomEnd = if (isUser) 2.dp else 14.dp
                                        )
                                    )
                                    .background(
                                        if (isUser) CoralPrimary
                                        else if (msg.sender == "system") AmberSecondary.copy(alpha = 0.2f)
                                        else SlateNavyDark
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    color = if (msg.sender == "system") SlateNavyDark else Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Ask support or report order issue...", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                viewModel.sendChatMessage(chatInput)
                                chatInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(CoralPrimary, CircleShape)
                            .testTag("send_chat_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        } else {
            // Support Tickets Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Open new ticket form
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Create Official Support Ticket",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = ticketOrderId,
                                onValueChange = { ticketOrderId = it },
                                label = { Text("Order ID (Optional)") },
                                placeholder = { Text("e.g. ORD-89412") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("ticket_order_id")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = ticketSubject,
                                onValueChange = { ticketSubject = it },
                                label = { Text("Issue Subject") },
                                placeholder = { Text("e.g. 3PL Shipment Tracking Query") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("ticket_subject")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = ticketMessage,
                                onValueChange = { ticketMessage = it },
                                label = { Text("Details & Description") },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth().testTag("ticket_message")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (ticketSubject.isNotBlank() && ticketMessage.isNotBlank()) {
                                        viewModel.createSupportTicket(
                                            orderId = ticketOrderId.ifBlank { null },
                                            subject = ticketSubject,
                                            category = ticketCategory,
                                            message = ticketMessage
                                        )
                                        ticketCreatedNotice = "Ticket submitted! Support desk investigating."
                                        ticketSubject = ""
                                        ticketMessage = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("submit_ticket_btn")
                            ) {
                                Text("Submit Priority Ticket", fontWeight = FontWeight.Bold)
                            }

                            if (ticketCreatedNotice != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ticketCreatedNotice!!,
                                    color = GreenSuccess,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Existing Tickets List
                items(tickets) { tck ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tck.ticketId,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralPrimary,
                                    fontSize = 13.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (tck.status) {
                                        "RESOLVED" -> GreenSuccess.copy(alpha = 0.15f)
                                        "IN_PROGRESS" -> AmberSecondary.copy(alpha = 0.2f)
                                        else -> CoralPrimary.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = tck.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (tck.status) {
                                            "RESOLVED" -> GreenSuccess
                                            "IN_PROGRESS" -> SlateNavyDark
                                            else -> CoralPrimary
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = tck.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (tck.orderId != null) {
                                Text(text = "Related Order: ${tck.orderId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = tck.lastMessage, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
