const searchForm = document.getElementById("search-form");
const results = document.getElementById("results");
const statusMessage = document.getElementById("status-message");
const resultsSummary = document.getElementById("results-summary");
const successModal = document.getElementById("success-modal");
const modalMessage = document.getElementById("modal-message");
const modalCloseButton = document.getElementById("modal-close-button");
const stationInputs = [
    document.getElementById("from-station"),
    document.getElementById("to-station")
];
const departureDateInput = document.getElementById("departure-date");
let allStations = [];

const formatDateTime = (value) =>
    new Intl.DateTimeFormat("en-GB", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));

function setStatus(message, type = "") {
    statusMessage.textContent = message;
    statusMessage.className = `status ${type}`.trim();
}

function setEmptyState(message) {
    results.innerHTML = `<div class="empty-state">${message}</div>`;
}

function showSuccessModal(message) {
    modalMessage.textContent = message;
    successModal.hidden = false;
    modalCloseButton.focus();
}

function closeSuccessModalAndRefresh() {
    successModal.hidden = true;
    window.location.reload();
}

async function loadStations() {
    const response = await fetch("/api/stations");
    allStations = await response.json();
}

function getOptionsContainer(input) {
    return document.querySelector(`[data-options-for="${input.id}"]`);
}

function renderStationOptions(input, stations, highlightedIndex = -1, shouldOpen = false) {
    const options = getOptionsContainer(input);
    if (stations.length === 0) {
        options.innerHTML = `<div class="station-option-empty">No matching stations</div>`;
        if (shouldOpen) {
            options.classList.add("open");
        }
        return;
    }

    options.innerHTML = stations
        .map((station, index) => `
            <button
                class="station-option ${index === highlightedIndex ? "active" : ""}"
                type="button"
                data-station-value="${station}">
                ${station}
            </button>
        `)
        .join("");
    if (shouldOpen) {
        options.classList.add("open");
    }

    options.querySelectorAll(".station-option").forEach((button) => {
        button.addEventListener("click", () => {
            input.value = button.dataset.stationValue;
            closeStationOptions(input);
        });
    });
}

function closeStationOptions(input) {
    getOptionsContainer(input).classList.remove("open");
}

function filterStations(query) {
    const normalized = query.trim().toLowerCase();
    if (!normalized) {
        return allStations;
    }

    return allStations.filter((station) =>
        station.toLowerCase().includes(normalized)
    );
}

stationInputs.forEach((input) => {
    let highlightedIndex = -1;

    input.addEventListener("focus", () => {
        highlightedIndex = -1;
        renderStationOptions(input, filterStations(input.value), highlightedIndex, true);
    });

    input.addEventListener("input", () => {
        highlightedIndex = -1;
        renderStationOptions(input, filterStations(input.value), highlightedIndex, true);
    });

    input.addEventListener("keydown", (event) => {
        const filteredStations = filterStations(input.value);
        if (!getOptionsContainer(input).classList.contains("open") && (event.key === "ArrowDown" || event.key === "ArrowUp")) {
            renderStationOptions(input, filteredStations, highlightedIndex, true);
        }

        if (event.key === "ArrowDown") {
            event.preventDefault();
            highlightedIndex = Math.min(highlightedIndex + 1, filteredStations.length - 1);
            renderStationOptions(input, filteredStations, highlightedIndex, true);
        }

        if (event.key === "ArrowUp") {
            event.preventDefault();
            highlightedIndex = Math.max(highlightedIndex - 1, 0);
            renderStationOptions(input, filteredStations, highlightedIndex, true);
        }

        if (event.key === "Enter" && highlightedIndex >= 0 && filteredStations[highlightedIndex]) {
            event.preventDefault();
            input.value = filteredStations[highlightedIndex];
            closeStationOptions(input);
        }

        if (event.key === "Escape") {
            closeStationOptions(input);
        }
    });
});

document.addEventListener("click", (event) => {
    stationInputs.forEach((input) => {
        const picker = input.closest("[data-station-picker]");
        if (!picker.contains(event.target)) {
            closeStationOptions(input);
        }
    });
});

async function searchConnections(fromStation, toStation, departureDate) {
    const query = new URLSearchParams({ fromStation, toStation, departureDate });
    const response = await fetch(`/api/search?${query.toString()}`);

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.detail || "Search failed.");
    }

    return response.json();
}

async function bookItinerary(scheduleIds, customerEmail, numberOfTickets, button) {
    if (!customerEmail) {
        setStatus("Enter an email address before booking a route.", "error");
        return;
    }

    button.disabled = true;
    setStatus("Booking...", "");

    try {
        const response = await fetch("/api/bookings/itinerary", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                scheduleIds,
                customerEmail,
                numberOfTickets
            })
        });

        const body = await response.json();
        if (!response.ok) {
            throw new Error(body.detail || "Booking failed.");
        }

        showSuccessModal(
            `Booked ${body.segmentCount} train segment(s) successfully. A confirmation email was sent to ${body.customerEmail}.`
        );
    } catch (error) {
        setStatus(error.message, "error");
    } finally {
        button.disabled = false;
    }
}

function renderResults(itineraries) {
    if (itineraries.length === 0) {
        setEmptyState("No routes found for this station pair.");
        return;
    }

    results.innerHTML = "";
    itineraries.forEach((itinerary) => {
        const card = document.createElement("article");
        card.className = "itinerary-card";

        card.innerHTML = `
            <div class="itinerary-meta">
                <div>
                    <span class="tag">${itinerary.numberOfChanges} change(s)</span>
                    <strong>${formatDateTime(itinerary.departureTime)} -> ${formatDateTime(itinerary.arrivalTime)}</strong>
                </div>
                <div>
                    <div>${itinerary.segments.length} train segment(s)</div>
                </div>
            </div>
            <div class="segments">
                ${itinerary.segments.map((segment) => `
                    <div class="segment">
                        <div class="segment-top">
                            <span>${segment.trainName}</span>
                            <span>${segment.fromStation} -> ${segment.toStation}</span>
                        </div>
                        <div class="segment-times">
                            ${formatDateTime(segment.departureTime)} -> ${formatDateTime(segment.arrivalTime)}
                        </div>
                    </div>
                `).join("")}
            </div>
            <div class="card-actions">
                <button class="book-button" type="button">Book this route</button>
            </div>
        `;

        const button = card.querySelector(".book-button");
        button.addEventListener("click", () => {
            const email = document.getElementById("customer-email").value;
            const ticketCount = Number(document.getElementById("ticket-count").value);
            bookItinerary(
                itinerary.segments.map((segment) => segment.scheduleId),
                email,
                ticketCount,
                button
            );
        });

        results.appendChild(card);
    });
}

searchForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(searchForm);
    const fromStation = formData.get("fromStation");
    const toStation = formData.get("toStation");
    const departureDate = formData.get("departureDate");

    stationInputs.forEach(closeStationOptions);
    setStatus("Searching routes...", "");
    setEmptyState("Looking for matching routes...");
    resultsSummary.textContent = `Searching routes from ${fromStation} to ${toStation} on ${departureDate}.`;

    try {
        const itineraries = await searchConnections(fromStation, toStation, departureDate);
        renderResults(itineraries);
        resultsSummary.textContent = `Found ${itineraries.length} route(s) from ${fromStation} to ${toStation} on ${departureDate}.`;
        setStatus(`Found ${itineraries.length} route(s).`, "success");
    } catch (error) {
        setEmptyState(`No route was found from ${fromStation} to ${toStation} on ${departureDate}.`);
        resultsSummary.textContent = `No route found for ${fromStation} -> ${toStation} on ${departureDate}.`;
        setStatus(error.message, "error");
    }
});

departureDateInput.value = "2026-05-25";
setEmptyState("Search for a route to see available journeys.");
loadStations().catch(() => {
    setStatus("Could not load stations.", "error");
});

modalCloseButton.addEventListener("click", closeSuccessModalAndRefresh);
