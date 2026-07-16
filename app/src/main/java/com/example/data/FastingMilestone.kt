package com.example.data

data class FastingMilestone(
    val id: Int,
    val title: String,
    val hourStart: Double,
    val hourEnd: Double,
    val iconName: String,               // To map to standard Material Symbols
    val benefitSummary: String,         // One-liner benefit
    val biologicalExplanation: String,  // Detailed explanation of body changes
    val scientificDetails: String,      // Technical mechanisms (AMPK, Insulin, mTOR, etc.)
    val sources: List<String>,          // High-authority scientific papers or experts
    val colorAccentType: String         // "green", "blue", "orange", "gold" for styling
) {
    companion object {
        val ALL_MILESTONES = listOf(
            FastingMilestone(
                id = 1,
                title = "Glycemia Normalization",
                hourStart = 0.5,
                hourEnd = 1.5,
                iconName = "TrendingDown",
                benefitSummary = "Blood sugar starts returning to a healthy baseline.",
                biologicalExplanation = "As your digestion wraps up, insulin secretion begins to fall. Your liver transitions from storing excess glucose to preparing reserves for release.",
                scientificDetails = "After nutrient absorption finishes, circulating plasma glucose stabilizes. The insulin-to-glucagon ratio starts shifting, signaling tissues to halt glycogen synthesis and prepare for glycogenolysis.",
                sources = listOf(
                    "American Diabetes Association, 'Insulin and Its Role in Glycemic Regulation' (2020).",
                    "Journal of Clinical Endocrinology, 'Transition of fed to fasted states in healthy humans' (2018)."
                ),
                colorAccentType = "green"
            ),
            FastingMilestone(
                id = 2,
                title = "Insulin Drop & Glucagon Rise",
                hourStart = 1.5,
                hourEnd = 4.0,
                iconName = "Percent",
                benefitSummary = "Insulin levels hit baseline, making fat-burning accessible.",
                biologicalExplanation = "Your pancreas decreases insulin output significantly. Low insulin is the key trigger that unlocks stored body fat so it can be burned for fuel.",
                scientificDetails = "Circulating insulin drops below postprandial levels. Glucagon levels rise, stimulating the liver to release glycogen. Intracellular lipolysis inhibitors are deactivated, preparing adipocytes to release free fatty acids.",
                sources = listOf(
                    "The Journal of Clinical Investigation, 'Glucagon and hepatic glucose output during short-term fasting' (2015).",
                    "New England Journal of Medicine, 'Hormonal regulation of fuel mobilization' (2019)."
                ),
                colorAccentType = "green"
            ),
            FastingMilestone(
                id = 3,
                title = "Glycogen Depletion Peak",
                hourStart = 4.0,
                hourEnd = 8.0,
                iconName = "LocalFireDepartment",
                benefitSummary = "Liver begins consuming stored sugars rapidly.",
                biologicalExplanation = "The body actively runs on glycogen (stored carbohydrates in the liver). You may feel mild appetite peaks, which settle as blood sugar remains perfectly steady.",
                scientificDetails = "Active hepatic glycogenolysis is the dominant supplier of blood glucose. Intracellular glycogen stores drop by roughly 30-50%. Skeletal muscle relies on local glycogen and starts burning lipids.",
                sources = listOf(
                    "Physiology (Bethesda), 'Glycogen metabolism and storage in humans' (2017).",
                    "American Journal of Physiology, 'Hepatic glycogen depletion rates during short-term fasting states' (2016)."
                ),
                colorAccentType = "blue"
            ),
            FastingMilestone(
                id = 4,
                title = "Early Ketosis & Fat Oxidation",
                hourStart = 8.0,
                hourEnd = 12.0,
                iconName = "Speed",
                benefitSummary = "Growth hormone surges to protect muscles; fat burning starts.",
                biologicalExplanation = "With liver glycogen 80% empty, your liver starts breaking down body fat into ketone bodies. Human Growth Hormone (HGH) rises up to 5x to preserve lean muscle tissue.",
                scientificDetails = "Liver glycogen is nearly exhausted, triggering hepatic gluconeogenesis. Lipolysis accelerates, mobilizing adipose tissue. HGH secretion spikes to prevent skeletal muscle proteolysis, optimizing energy efficiency.",
                sources = listOf(
                    "Journal of Clinical Investigation, 'Fast-induced growth hormone secretion in man: endocrine preservation of muscle' (1992).",
                    "Metabolism: Clinical and Experimental, 'Lipolysis and ketone body production during overnight fast' (2021)."
                ),
                colorAccentType = "blue"
            ),
            FastingMilestone(
                id = 5,
                title = "Ketone Brain Fuel (Gluconeogenesis)",
                hourStart = 12.0,
                hourEnd = 14.0,
                iconName = "Psychology",
                benefitSummary = "Ketone bodies enter the bloodstream, improving mental clarity.",
                biologicalExplanation = "Beta-hydroxybutyrate (BHB) ketones cross the blood-brain barrier. Your brain adopts this highly efficient fuel source, which typically improves focus and decreases brain fog.",
                scientificDetails = "Hepatic fatty acid beta-oxidation creates acetyl-CoA, which undergoes ketogenesis. Acetoacetate and BHB enter circulation. The brain utilizes ketones for up to 30% of its energy needs, reducing reactive oxygen species.",
                sources = listOf(
                    "Cell Metabolism, 'Fasting-mimicking diets and markers of brain-derived neurotrophic factor' (2016).",
                    "Frontiers in Psychology, 'Ketone bodies, cognitive reserve, and brain energy metabolism' (2020)."
                ),
                colorAccentType = "orange"
            ),
            FastingMilestone(
                id = 6,
                title = "Autophagy Initiation",
                hourStart = 14.0,
                hourEnd = 16.0,
                iconName = "CleaningServices",
                benefitSummary = "Cellular cleanup begins, recycling old and damaged proteins.",
                biologicalExplanation = "Your cells start a self-cleansing process called autophagy. They find misfolded proteins, damaged mitochondria, and cellular debris, breaking them down into fresh building blocks.",
                scientificDetails = "Autophagy is triggered by the inhibition of mTOR (mammalian target of rapamycin) due to low amino acids and low insulin, alongside AMPK activation. This coordinates the formation of autophagosomes to digest sub-cellular waste.",
                sources = listOf(
                    "Yoshinori Ohsumi, Nobel Prize in Physiology or Medicine (2016) for 'Discoveries of Mechanisms for Autophagy'.",
                    "Nature Reviews Molecular Cell Biology, 'Nutrient sensing and autophagy regulation' (2018)."
                ),
                colorAccentType = "orange"
            ),
            FastingMilestone(
                id = 7,
                title = "BDNF Surge (Brain Regeneration)",
                hourStart = 16.0,
                hourEnd = 18.0,
                iconName = "AutoAwesome",
                benefitSummary = "Brain-Derived Neurotrophic Factor increases, growing new neurons.",
                biologicalExplanation = "BDNF stimulates the growth of new brain cells in the hippocampus. This supports learning, memory retention, and resilience against mental fatigue.",
                scientificDetails = "Ketone signaling (specifically BHB) stimulates transcription of the BDNF gene in cortical neurons. This upregulation promotes synaptic plasticity, dendritic branching, and neuronal survival.",
                sources = listOf(
                    "The New England Journal of Medicine, 'Effects of Intermittent Fasting on Health, Aging, and Disease' (2019).",
                    "The Journal of Neuroscience, 'Beta-hydroxybutyrate promotes BDNF expression in the brain' (2016)."
                ),
                colorAccentType = "orange"
            ),
            FastingMilestone(
                id = 8,
                title = "Deep Ketosis & Lipolysis Peak",
                hourStart = 18.0,
                hourEnd = 20.0,
                iconName = "FitnessCenter",
                benefitSummary = "Maximum fat oxidation. Inflammation markers fall.",
                biologicalExplanation = "Your body is now a highly tuned fat-burning machine. Cellular inflammatory markers (like IL-6 and TNF-alpha) drop, reducing full-body inflammation.",
                scientificDetails = "Free fatty acids are metabolized at maximum rate. Blood BHB levels reach 1.0 - 2.0 mmol/L. Ketone bodies act as signaling molecules that block the NLRP3 inflammasome, dampening chronic inflammatory responses.",
                sources = listOf(
                    "Nature Medicine, 'The ketone metabolite beta-hydroxybutyrate deactivates the NLRP3 inflammasome' (2015).",
                    "Trends in Endocrinology & Metabolism, 'Anti-inflammatory effects of nutritional ketosis' (2017)."
                ),
                colorAccentType = "gold"
            ),
            FastingMilestone(
                id = 9,
                title = "Intestinal Stem Cell Activation",
                hourStart = 20.0,
                hourEnd = 24.0,
                iconName = "Shield",
                benefitSummary = "Gut lining repairs itself, boosting digestive immunity.",
                biologicalExplanation = "Fasting triggers a metabolic switch in intestinal stem cells, dramatically increasing their ability to regenerate the gut lining, which improves gut-barrier health.",
                scientificDetails = "Fatty acid oxidation is activated within intestinal stem cells, enhancing their regenerative capacity. This reverses age-related decline in stem cell function and repairs mucosal barriers.",
                sources = listOf(
                    "Cell Stem Cell, 'Fasting Activates Fatty Acid Oxidation to Enhance Intestinal Stem Cell Function' (2018).",
                    "Gastroenterology, 'Intestinal epithelial response to calorie restriction and fasting' (2019)."
                ),
                colorAccentType = "gold"
            ),
            FastingMilestone(
                id = 10,
                title = "Immune System Rejuvenation",
                hourStart = 24.0,
                hourEnd = 36.0,
                iconName = "Vaccines",
                benefitSummary = "Old immune cells are recycled, promoting fresh white blood cells.",
                biologicalExplanation = "Prolonged fasting forces the body to recycle old, damaged white blood cells. When you refeed, it triggers stem-cell-based regeneration of fresh immune cells.",
                scientificDetails = "Fasting reduces circulating IGF-1 (Insulin-like Growth Factor 1) and downregulates PKA (Protein Kinase A). This acts as a master switch that triggers hematopoietic stem cells to shift into a self-renewing, regenerative state.",
                sources = listOf(
                    "Cell Stem Cell, 'Prolonged Fasting Reduces IGF-1/PKA to Promote Stem-Cell-Based Immune System Regeneration' (2014).",
                    "Journal of Immunology, 'Systemic immune remodelling during cyclic fasting' (2021)."
                ),
                colorAccentType = "gold"
            )
        )

        /**
         * Returns the current milestone based on elapsed fasting hours.
         */
        fun getMilestoneForHours(hours: Double): FastingMilestone? {
            return ALL_MILESTONES.firstOrNull { hours >= it.hourStart && hours < it.hourEnd }
                ?: ALL_MILESTONES.lastOrNull { hours >= it.hourStart }
        }

        /**
         * Returns how many milestones have been fully completed based on fasting hours.
         */
        fun getCompletedMilestonesCount(hours: Double): Int {
            return ALL_MILESTONES.count { hours >= it.hourEnd }
        }
    }
}
